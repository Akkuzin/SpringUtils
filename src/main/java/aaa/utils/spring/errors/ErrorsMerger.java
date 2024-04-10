package aaa.utils.spring.errors;

import aaa.basis.text.StringFunc;
import aaa.lang.reflection.ReflectionUtils;
import aaa.lang.reflection.ReflectionUtils.SerializableBiConsumer;
import aaa.lang.reflection.ReflectionUtils.SerializableFunction;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class ErrorsMerger {

  private static final String DELIMITER = ".";

  /**
   * Append errors messages
   *
   * @param errors Main errors collection
   * @param newErrors Additional errors collection
   * @param relativePath Path of newErrors object in errors object
   */
  public static void mergeErrors(Errors errors, Errors newErrors, String relativePath) {
    // Global errors
    for (ObjectError e : newErrors.getGlobalErrors()) {
      errors.reject(e.getCode(), e.getArguments(), e.getDefaultMessage());
    }
    // Field Errors
    for (FieldError e : newErrors.getFieldErrors()) {
      errors.rejectValue(
          StringFunc.concatWithDelim(relativePath, DELIMITER, e.getField()),
          e.getCode(),
          e.getArguments(),
          e.getDefaultMessage());
    }
  }

  public static void mergeErrorsToGlobal(Errors errors, Errors newErrors) {
    // Global errors
    for (ObjectError e : newErrors.getGlobalErrors()) {
      errors.reject(e.getCode(), e.getArguments(), e.getDefaultMessage());
    }
    // Field Errors
    for (FieldError e : newErrors.getFieldErrors()) {
      errors.reject(e.getCode(), e.getArguments(), e.getDefaultMessage());
    }
  }

  public static void mergeErrorsToField(
      Errors errors, Errors newErrors, SerializableFunction getter) {
    mergeErrorsToField(errors, newErrors, ReflectionUtils.propertyNameFor(getter));
  }

  public static void mergeErrorsToField(
      Errors errors, Errors newErrors, SerializableBiConsumer setter) {
    mergeErrorsToField(errors, newErrors, ReflectionUtils.propertyNameFor(setter));
  }

  public static void mergeErrorsToField(Errors errors, Errors newErrors, String relativePath) {
    // Global errors
    for (ObjectError e : newErrors.getGlobalErrors()) {
      errors.rejectValue(relativePath, e.getCode(), e.getArguments(), e.getDefaultMessage());
    }
    // Field Errors
    for (FieldError e : newErrors.getFieldErrors()) {
      errors.rejectValue(
          StringFunc.concatWithDelim(relativePath, DELIMITER, e.getField()),
          e.getCode(),
          e.getArguments(),
          e.getDefaultMessage());
    }
  }

  public static String asString(Errors errors) {
    StringBuilder result = new StringBuilder();
    if (errors != null) {
      for (ObjectError e : errors.getGlobalErrors()) {
        result.append(
            "error object "
                + e.getObjectName()
                + ": "
                + e.getCode()
                + "("
                + e.getArguments()
                + ") // "
                + e.getDefaultMessage());
      }
      for (FieldError e : errors.getFieldErrors()) {
        result.append(
            "error field "
                + e.getField()
                + ": "
                + e.getCode()
                + "("
                + e.getArguments()
                + ") // "
                + e.getDefaultMessage());
      }
    }
    return result.toString();
  }
}
