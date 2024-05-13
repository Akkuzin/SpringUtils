package aaa.utils.spring.errors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class BaseValidator<T> implements Validator {

  public static final int MAX_LARGE_TEXT_LENGTH = 2000;
  public static final int MAX_ENTITY_NAME_LENGTH = 250;
  public static final int MAX_ENTITY_CODE_LENGTH = 250;

  public static final int SYSTEM_ID_LENGTH = 19;

  protected Log log = LogFactory.getLog(this.getClass());

  public Errors validateForErrors(Object target) {
    return validateForErrors(this, target);
  }

  public static Errors validateForErrors(Validator validator, Object target) {
    Errors errors = ServiceExceptionFactory.init(target).toErrors();
    validator.validate(target, errors);
    return errors;
  }
}
