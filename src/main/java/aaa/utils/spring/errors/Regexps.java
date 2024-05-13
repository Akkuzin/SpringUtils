package aaa.utils.spring.errors;

import java.util.regex.Pattern;

public class Regexps {

  //CHECKSTYLE:OFF

  //  public static final String EMAIL_PATTERN =
  //      "[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*"
  //        + "@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?";
  private static final String EMAIL_BASE_PATTERN = "[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
      + "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+";
  public static final String EMAIL_PATTERN = "^" + EMAIL_BASE_PATTERN + "$";
  public static final String EMAILS_PATTERN =
      "^[;\\s]*" + EMAIL_BASE_PATTERN + "([;\\s]+(" + EMAIL_BASE_PATTERN + ")*)*$";
  public static final String PHONE_PATTERN = "(^\\d{10}$)|(^(\\+|)\\d{11}$)";
  public static final String INN_PATTERN = "(^\\d{10}$)|(^F\\d{10}$)|(^\\d{12}$)";
  public static final String DOTTED_PROPERTY_PATH_PATTERN =
      "(([A-Za-z])+([A-Za-z0-9])*\\.)*([A-Za-z])+([A-Za-z0-9])*";

  //CHECKSTYLE:ON

  /**
   * Check where or not string satisfies pattern
   * 
   * @param string
   *          String to be matched
   * @param patternString
   *          String containing pattern for matching
   * @return
   */
  public static boolean isMatches(String string, String patternString) {
    return Pattern.matches(patternString, string);
  }

  /**
   * Check where or not string satisfies patterns
   * 
   * @param string
   *          String to be matched
   * @param patterns
   *          String array containing patterns for matching
   * @return
   */
  public static boolean isMatches(String string, String[] patterns) {
    if (patterns == null) {
      return true;
    }
    for (String pattern : patterns) {
      if (isMatches(string, pattern)) {
        return true;
      }
    }
    return false;
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
