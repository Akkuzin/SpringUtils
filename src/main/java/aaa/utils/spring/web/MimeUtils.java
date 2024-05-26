package aaa.utils.spring.web;

import static java.util.Optional.ofNullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;

public class MimeUtils {

  private static final Detector DETECTOR = new DefaultDetector();

  public static String getMimeType(byte[] data, String filename) {
    return getMimeType(data == null ? null : new ByteArrayInputStream(data), filename);
  }

  public static String getMimeType(InputStream data, String filename) {
    try {
      Metadata metadata = new Metadata();
      metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, filename);
      return ofNullable(DETECTOR.detect(data, metadata)).map(String::valueOf).orElse("");
    } catch (Throwable e) {
      return "";
    } finally {
      try {
        if (data != null) {
          data.reset();
        }
      } catch (IOException e) {
        return "";
      }
    }
  }

  @SuppressWarnings("checkstyle:IllegalCatch")
  public static String getExtensionForMimeType(String mimeType) {
    return ofNullable(mimeType)
        .filter(StringUtils::isNotBlank)
        .map(
            type -> {
              try {
                return MimeTypes.getDefaultMimeTypes().forName(type);
              } catch (Throwable e) {
                return null;
              }
            })
        .map(MimeType::getExtension)
        .orElse("");
  }
}
