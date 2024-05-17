package aaa.utils.spring.errors;

import static aaa.utils.spring.integration.jpa.AbstractPOJOUtils.getPojoClass;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@AllArgsConstructor
public class ServiceExceptionFactory {

  private String context;
  private Errors errors;

  public static ServiceExceptionFactory init(Object object) {
    return init(null, object);
  }

  public static ServiceExceptionFactory init(String context, Object object) {
    return new ServiceExceptionFactory(
        context,
        object == null
            ? new BeanPropertyBindingResult("", "")
            : new BeanPropertyBindingResult(
                object, uncapitalize(getPojoClass(object).getSimpleName())));
  }

  public synchronized ServiceExceptionFactory addError(String errorCode, String defaultMessage) {
    errors.reject(errorCode, null, defaultMessage);
    return this;
  }

  public synchronized ServiceExceptionFactory addError(
      String errorCode, Object[] errorArgs, String defaultMessage) {
    errors.reject(errorCode, errorArgs, defaultMessage);
    return this;
  }

  public synchronized ServiceExceptionFactory addError(
      String errorCode, Map<String, ?> parameterMap, String defaultMessage) {
    errors.reject(
        errorCode,
        parameterMap == null ? new Object[] {} : new Object[] {parameterMap},
        defaultMessage);
    return this;
  }

  public synchronized ServiceExceptionFactory mergeErrors(Errors newErrors, String relativePath) {
    ErrorsMerger.mergeErrors(errors, newErrors, relativePath);
    return this;
  }

  public synchronized ServiceExceptionFactory mergeErrorsToGlobal(Errors newErrors) {
    ErrorsMerger.mergeErrorsToGlobal(errors, newErrors);
    return this;
  }

  public synchronized ServiceExceptionFactory mergeErrorsToField(
      Errors newErrors, String relativePath) {
    ErrorsMerger.mergeErrorsToField(errors, newErrors, relativePath);
    return this;
  }

  public synchronized boolean hasErrors() {
    return errors.hasErrors();
  }

  public EntityServiceException make() {
    return new EntityServiceException(context, errors);
  }

  /** Бросает исключение, если есть ошибки. */
  public void throwIfHasErrors() {
    if (hasErrors()) {
      throw make();
    }
  }

  public Errors toErrors() {
    return errors;
  }
}
