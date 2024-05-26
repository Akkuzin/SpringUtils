package aaa.utils.spring.errors;

import static com.google.common.collect.Streams.concat;
import static java.util.stream.Collectors.joining;

import aaa.basis.text.StringFunc;
import aaa.lang.reflection.ReflectionUtils;
import aaa.lang.reflection.ReflectionUtils.SerializableBiConsumer;
import aaa.lang.reflection.ReflectionUtils.SerializableFunction;
import org.springframework.validation.Errors;

public class ErrorsMerger {

  private static final String DELIMITER = ".";

  /** Append errors messages */
  public static void mergeErrors(Errors errors, Errors newErrors, String relativePath) {
    newErrors
        .getGlobalErrors()
        .forEach(e -> errors.reject(e.getCode(), e.getArguments(), e.getDefaultMessage()));
    newErrors
        .getFieldErrors()
        .forEach(
            e ->
                errors.rejectValue(
                    StringFunc.concatWithDelim(relativePath, DELIMITER, e.getField()),
                    e.getCode(),
                    e.getArguments(),
                    e.getDefaultMessage()));
  }

  public static void mergeErrorsToGlobal(Errors errors, Errors newErrors) {
    newErrors
        .getGlobalErrors()
        .forEach(e -> errors.reject(e.getCode(), e.getArguments(), e.getDefaultMessage()));
    newErrors
        .getFieldErrors()
        .forEach(e -> errors.reject(e.getCode(), e.getArguments(), e.getDefaultMessage()));
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
    newErrors
        .getGlobalErrors()
        .forEach(
            e ->
                errors.rejectValue(
                    relativePath, e.getCode(), e.getArguments(), e.getDefaultMessage()));
    newErrors
        .getFieldErrors()
        .forEach(
            e ->
                errors.rejectValue(
                    StringFunc.concatWithDelim(relativePath, DELIMITER, e.getField()),
                    e.getCode(),
                    e.getArguments(),
                    e.getDefaultMessage()));
  }

  public static String asString(Errors errors) {
    return errors == null
        ? ""
        : concat(
                errors.getGlobalErrors().stream()
                    .map(
                        e ->
                            String.format(
                                "error object %s: %s (%s) // %s",
                                e.getObjectName(),
                                e.getCode(),
                                e.getArguments(),
                                e.getDefaultMessage())),
                errors.getFieldErrors().stream()
                    .map(
                        e ->
                            String.format(
                                "error field %s: %s (%s) // %s",
                                e.getField(),
                                e.getCode(),
                                e.getArguments(),
                                e.getDefaultMessage())))
            .collect(joining("\n"));
  }
}
