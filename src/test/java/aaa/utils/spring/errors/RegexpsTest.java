package aaa.utils.spring.errors;

import static aaa.utils.spring.errors.Regexps.EMAILS_PATTERN;
import static aaa.utils.spring.errors.Regexps.isMatches;
import static aaa.utils.spring.errors.Regexps.validateEmail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RegexpsTest {

  @Test
  public void testValidateEmail() {
    assertTrue(validateEmail("director@some-company.ru"));
    assertTrue(validateEmail("mary@other.company.com"));

    assertFalse(validateEmail("lun@flsymacsda.se."));
    assertFalse(validateEmail("prti.tevinn@gmail.com."));
    assertFalse(validateEmail("<Zoran>Radunovic@albatrosairways.net"));
    assertFalse(validateEmail("n/a SITA: n/a"));
    assertFalse(validateEmail("vdsserearfcvim@amifrdmeafcsdau.com.mo*"));
    assertFalse(validateEmail("CSDACCA.COM.ZZ"));
  }

  @Test
  public void testValidateEmails() {
    assertTrue(isMatches("director@some-company.ru;mary@other.company.com", EMAILS_PATTERN));
    assertTrue(isMatches(";director@some-company.ru;mary@other.company.com;", EMAILS_PATTERN));
    assertTrue(
        isMatches(
            ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;"
                + "mary@other.company.com;",
            EMAILS_PATTERN));
  }
}
