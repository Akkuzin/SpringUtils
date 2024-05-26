package aaa.utils.spring.errors;

import java.util.stream.Stream;

public class Regexps {

  // CHECKSTYLE:OFF

  public static final String EMAIL_RFC_PATTERN =
      "[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*"
          + "@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?";
  private static final String EMAIL_BASE_PATTERN =
      "[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+";
  public static final String EMAIL_PATTERN = "^" + EMAIL_BASE_PATTERN + "$";
  public static final String EMAILS_PATTERN =
      "^[;\\s]*" + EMAIL_BASE_PATTERN + "([;\\s]+(" + EMAIL_BASE_PATTERN + ")*)*$";
  public static final String PHONE_PATTERN = "(^\\d{10}$)|(^(\\+|)\\d{11}$)";
  public static final String INN_PATTERN = "(^\\d{10}$)|(^F\\d{10}$)|(^\\d{12}$)";

  // CHECKSTYLE:ON

  /** Check where or not string satisfies all patterns */
  public static boolean isMatches(String string, String... patterns) {
    return string != null
        && (patterns == null || Stream.of(patterns).anyMatch(pattern -> string.matches(pattern)));
  }

  /** Check where or not email address is valid */
  public static boolean validateEmail(String email) {
    return isMatches(email, EMAIL_PATTERN);
  }

  /** Check where or not phone number is valid */
  public static boolean validatePhoneNumber(String phone) {
    return isMatches(phone, PHONE_PATTERN);
  }

  /** Check where or not INN format is valid */
  public static boolean validateInn(String inn) {
    return isMatches(inn, INN_PATTERN);
  }
}
