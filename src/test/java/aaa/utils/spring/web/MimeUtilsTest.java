package aaa.utils.spring.web;

import static aaa.utils.spring.web.MimeUtils.getMimeType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class MimeUtilsTest {

  @Test
  @SneakyThrows
  public void testGetMimeType() {
    assertEquals(
        "application/pdf",
        getMimeType(
            IOUtils.toByteArray(MimeUtils.class.getResourceAsStream("/blanks/noDoc.pdf")),
            "name.xml"));
    assertEquals("application/pdf", getMimeType(new byte[] {}, "name.pdf"));
    assertEquals("application/pdf", getMimeType((byte[]) null, "name.pdf"));
  }
}
