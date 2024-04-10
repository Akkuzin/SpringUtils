package aaa.utils.files.download;

import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.lowerCase;

import aaa.web.http.UrlUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.util.FileCopyUtils;

public class HttpUtils {

  static final String FILENAME = "filename";
  static final String FILENAME_ENCODING = StandardCharsets.UTF_8.name();
  static final String CHROME = "chrome";
  static final String MSIE = "msie";
  static final String USER_AGENT = "User-Agent";
  static final String CONTENT_DISPOSITION = "Content-Disposition";

  static final String IE_TEMPLATE = FILENAME + "=\"%s\"";
  static final String FIREFOX_TEMPLATE = FILENAME + "*=" + FILENAME_ENCODING + "''%s";

  protected static String makeContentDisposition(String filename, String userAgent)
      throws UnsupportedEncodingException {
    String filenameHeader = null;
    // To inspect details for the below code, see http://greenbytes.de/tech/tc2231/

    String preparedName = UrlUtils.prepareString(filename, FILENAME_ENCODING, null);
    if (contains(userAgent, MSIE) || contains(userAgent, CHROME)) {
      // IE does not support internationalized filename at all.
      // It can only recognize internationalized URL, so we do the trick via routing rules.
      filenameHeader = String.format(IE_TEMPLATE, preparedName);
      //    } else if (contains(ag, "webkit")) {
      //      // Safari 3.0 and Chrome 2.0 accepts UTF-8 encoded string directly.
      //      filenameHeader = "filename=" + filename;
    } else {
      // For others like Firefox, we follow RFC2231 (encoding extension in HTTP headers).
      filenameHeader = String.format(FIREFOX_TEMPLATE, preparedName);
    }
    return filenameHeader;
  }

  public static class DataSender {
    String filename;
    InputStream data;
    int dataLength;
    HttpServletResponse response;
    String userAgent;
    String mimeType;
    ServletContext servletContext;

    protected DataSender() {}

    public static DataSender init() {
      return new DataSender();
    }

    public static DataSender init(
        @NonNull ServletContext servletContext,
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response) {
      return new DataSender().inContext(servletContext).forClient(request).toResponse(response);
    }

    public DataSender toResponse(@NonNull HttpServletResponse response) {
      this.response = response;
      return this;
    }

    public DataSender forClient(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }

    public DataSender forClient(@NonNull HttpServletRequest request) {
      this.userAgent = request.getHeader(USER_AGENT);
      return this;
    }

    public DataSender mimeType(@NonNull String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    public DataSender data(byte[] data) {
      if (data == null) {
        data(new ByteArrayInputStream(new byte[0]), 0);
      } else {
        data(new ByteArrayInputStream(data), data.length);
      }
      return this;
    }

    public DataSender data(@NonNull InputStream data, int dataLength) {
      this.data = data;
      this.dataLength = dataLength;
      return this;
    }

    public DataSender withName(@NonNull String filename) {
      this.filename = getName(filename);
      return this;
    }

    public DataSender inContext(@NonNull ServletContext servletContext) {
      this.servletContext = servletContext;
      return this;
    }

    @SneakyThrows
    public void send() {
      if (mimeType != null) {
        response.setContentType(mimeType);
      } else if (servletContext != null) {
        response.setContentType(servletContext.getMimeType(filename));
      }
      response.setContentLength(dataLength);
      response.setHeader(
          CONTENT_DISPOSITION,
          "attachment; " + makeContentDisposition(filename, lowerCase(userAgent)));
      ServletOutputStream outputStream = response.getOutputStream();
      FileCopyUtils.copy(data, outputStream);
      outputStream.close();
    }

    public void send(byte[] data) {
      this.data(data).send();
    }

    public void send(Optional<byte[]> data) {
      data.ifPresent(this::send);
    }
  }
}
