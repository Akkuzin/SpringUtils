package aaa.utils.files.download;

import static aaa.nvl.Nvl.nvl;
import static aaa.nvl.Nvl.nvlGet;
import static org.apache.commons.lang3.StringUtils.equalsAny;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import aaa.format.SafeParse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

@Builder
public class MultipartFileSender {

  private static final int DEFAULT_BUFFER_SIZE = 20480; // ..bytes = 20KB.
  private static final long DEFAULT_EXPIRE_TIME = 604800000L; // ..ms = 1 week.
  private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

  @NonNull String fileName;
  @NonNull Long length;
  @NonNull Instant lastModifiedTime;
  @NonNull BufferedInputStream input;
  @NonNull HttpServletRequest request;
  @NonNull HttpServletResponse response;
  @Builder.Default int bufferSize = DEFAULT_BUFFER_SIZE;
  @Builder.Default long expireTime = DEFAULT_EXPIRE_TIME;

  @SneakyThrows
  public void serveResource() {
    if (StringUtils.isEmpty(fileName) || lastModifiedTime == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    long lastModified = lastModifiedTime.toEpochMilli() / 1000;
    String contentType = request.getServletContext().getMimeType(fileName);

    // Validate request headers for caching ---------------------------------------------------

    // If-None-Match header should contain "*" or ETag. If so, then return 304.
    String ifNoneMatch = request.getHeader("If-None-Match");
    if (ifNoneMatch != null && HttpUtils.matches(ifNoneMatch, fileName)) {
      response.setHeader("ETag", fileName); // Required in 304.
      response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }

    // If-Modified-Since header should be greater than LastModified. If so, then return 304.
    // This header is ignored if any If-None-Match header is specified.
    long ifModifiedSince = request.getDateHeader("If-Modified-Since");
    if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
      response.setHeader("ETag", fileName); // Required in 304.
      response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }

    // Validate request headers for resume ----------------------------------------------------

    // If-Match header should contain "*" or ETag. If not, then return 412.
    String ifMatch = request.getHeader("If-Match");
    if (ifMatch != null && !HttpUtils.matches(ifMatch, fileName)) {
      response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
      return;
    }

    // If-Unmodified-Since header should be greater than LastModified. If not, then return 412.
    long ifUnmodifiedSince = request.getDateHeader("If-Unmodified-Since");
    if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
      response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
      return;
    }

    // Validate and process range -------------------------------------------------------------

    // Prepare some variables. The full Range represents the complete file.
    Range full = new Range(0, length - 1, length);
    List<Range> ranges = new ArrayList<>();

    // Validate and process Range and If-Range headers.
    String rangeHeader = request.getHeader("Range");
    if (rangeHeader != null) {

      // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
      if (!rangeHeader.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
        response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
        return;
      }

      String ifRange = request.getHeader("If-Range");
      if (ifRange != null && !ifRange.equals(fileName)) {
        try {
          if (request.getDateHeader("If-Range") != -1) { // Throws IAE if invalid.
            ranges.add(full);
          }
        } catch (IllegalArgumentException ignore) {
          ranges.add(full);
        }
      }

      // If any valid If-Range header, then process each part of byte range.
      if (ranges.isEmpty()) {
        for (String part : rangeHeader.substring(6).split(",")) {
          // Assuming a file with length of 100, the following examples returns bytes at:
          // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
          Long start = SafeParse.parseLong(substringBefore(part, "-"), -1L);
          Long end = SafeParse.parseLong(substringAfter(part, "-"), -1L);

          if (start == -1) {
            start = length - end;
            end = length - 1;
          } else if (end == -1 || end > length - 1) {
            end = length - 1;
          }

          // Check if Range is syntactically valid. If not, then return 416.
          if (start > end) {
            response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
            response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
          }

          // Add range.
          ranges.add(new Range(start, end, length));
        }
      }
    }

    // Prepare and initialize response --------------------------------------------------------

    // Get content type by file name and set content disposition.
    String disposition = "inline";

    // If content type is unknown, then set the default value.
    // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
    // To add new content types, add new mime-mapping entry in web.xml.
    if (contentType == null) {
      contentType = "application/octet-stream";
    } else if (!contentType.startsWith("image")) {
      // Else, expect for images, determine content disposition. If content type is supported by
      // the browser, then set to inline, else attachment which will pop a 'save as' dialogue.
      String accept = request.getHeader("Accept");
      disposition =
          accept != null && HttpUtils.accepts(accept, contentType) ? "inline" : "attachment";
    }
    // Initialize response.
    response.reset();
    response.setBufferSize(bufferSize);
    response.setHeader("Content-Type", contentType);
    response.setHeader("Content-Disposition", disposition + ";filename=\"" + fileName + "\"");
    response.setHeader("Accept-Ranges", "bytes");
    response.setHeader("ETag", fileName);
    response.setDateHeader("Last-Modified", lastModified);
    response.setDateHeader("Expires", System.currentTimeMillis() + expireTime);

    // Send requested file (part(s)) to client ------------------------------------------------

    // Prepare streams.
    try (InputStream input = this.input;
        ServletOutputStream output = response.getOutputStream()) {

      if (ranges.isEmpty() || ranges.size() == 1) {

        // Return single part of file.
        Range range = nvl(nvlGet(ranges, 0), full);
        response.setContentType(contentType);
        response.setHeader(
            "Content-Range", "bytes " + range.start + "-" + range.end + "/" + range.total);
        response.setHeader("Content-Length", String.valueOf(range.length));
        if (!range.isFull()) {
          response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.
        }

        // Copy single part range.
        range.copy(input, output, bufferSize);

      } else {

        // Return multiple parts of file.
        response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

        // Copy multi part range.
        for (Range range : ranges) {
          // Add multipart boundary and header fields for every range.
          output.println();
          output.println("--" + MULTIPART_BOUNDARY);
          output.println("Content-Type: " + contentType);
          output.println(
              "Content-Range: bytes " + range.start + "-" + range.end + "/" + range.total);

          // Copy single part range of multi part range.
          range.copy(input, output, bufferSize);
        }

        // End with multipart boundary.
        output.println();
        output.println("--" + MULTIPART_BOUNDARY + "--");
      }
    }
  }

  static class Range {

    long start; // Start of the byte range.
    long end; // End of the byte range.
    long length;
    long total; // Total length of the byte source.

    /** Construct a byte range */
    public Range(long start, long end, long total) {
      this.start = start;
      this.end = end;
      this.length = end - start + 1;
      this.total = total;
    }

    public boolean isFull() {
      return length == total;
    }

    private void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
      byte[] buffer = new byte[bufferSize];
      IOUtils.copyLarge(input, output, start, length, buffer);
    }
  }

  static class HttpUtils {

    static Stream<String> splitHeader(String header) {
      return Stream.of(header.split(",")).map(StringUtils::strip);
    }

    static boolean headerMatches(String header, String... toAccept) {
      return splitHeader(header).anyMatch(value -> equalsAny(value, toAccept));
    }

    static boolean accepts(String acceptHeader, String toAccept) {
      return headerMatches(
          acceptHeader, new String[] {toAccept, "*", toAccept.replaceAll("/.*$", "/*")});
    }

    static boolean matches(String matchHeader, String toMatch) {
      return headerMatches(matchHeader, new String[] {toMatch, "*"});
    }
  }
}
