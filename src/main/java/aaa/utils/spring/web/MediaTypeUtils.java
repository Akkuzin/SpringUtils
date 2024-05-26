package aaa.utils.spring.web;

import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;

public class MediaTypeUtils {

  static final String CHARSET = ";charset=" + StandardCharsets.UTF_8;

  public static final MediaType APPLICATION_XML =
      new MediaType(
          MediaType.APPLICATION_XML.getType(),
          MediaType.APPLICATION_XML.getSubtype(),
          StandardCharsets.UTF_8);
  public static final String APPLICATION_XML_VALUE = MediaType.APPLICATION_XML_VALUE + CHARSET;

  public static final MediaType APPLICATION_JSON =
      new MediaType(
          MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          StandardCharsets.UTF_8);
  public static final String APPLICATION_JSON_VALUE = MediaType.APPLICATION_JSON_VALUE + CHARSET;

  public static final MediaType TEXT_PLAIN =
      new MediaType(
          MediaType.TEXT_PLAIN.getType(),
          MediaType.TEXT_PLAIN.getSubtype(),
          StandardCharsets.UTF_8);
  public static final String TEXT_PLAIN_VALUE = MediaType.TEXT_PLAIN_VALUE + CHARSET;

  public static final MediaType TEXT_HTML =
      new MediaType(
          MediaType.TEXT_HTML.getType(), MediaType.TEXT_HTML.getSubtype(), StandardCharsets.UTF_8);
  public static final String TEXT_HTML_VALUE = MediaType.TEXT_HTML_VALUE + CHARSET;
}
