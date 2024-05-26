package aaa.utils.spring.errors;

import static aaa.nvl.Nvl.nvl;
import static aaa.nvl.Nvl.nvlToString;
import static org.apache.commons.lang3.StringUtils.containsOnly;
import static org.apache.commons.lang3.StringUtils.isBlank;

import aaa.basis.text.StringFunc;
import aaa.utils.spring.template.ParamsFactory;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.Errors;

@FieldDefaults(level = AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class FieldValidator<T, F extends FieldValidator<T, F>> {

  public static final ErrorInfo API_NOT_NULL =
      ErrorInfo.code("api.not_null")
          .defaultText("Поле \"$i18n.resolveCode($name)\" является обязательным для заполнения")
          .build();
  public static final ErrorInfo API_SYMBOLS_SET =
      ErrorInfo.code("api.symbols_set")
          .defaultText("Поле \"${i18n.resolveCode($name)}\" содержит недопустимые символы")
          .build();
  public static final ErrorInfo API_LENGTH_TOO_LONG =
      ErrorInfo.code("api.length_too_long")
          .defaultText(
              "Длина значения поля \"${i18n.resolveCode($name)}\" должна быть не более $length")
          .build();
  public static final ErrorInfo API_LENGTH_TOO_SHORT =
      ErrorInfo.code("api.length_too_short")
          .defaultText(
              "Длина значения поля \"${i18n.resolveCode($name)}\" должна быть не менее $length")
          .build();

  @NonNull final FieldInfo<? extends T> fieldInfo;
  @NonNull final Errors errors;
  final T value;
  final String nestedPath;

  boolean skipFollowing;
  boolean failed;

  public F failed() {
    failed = true;
    return (F) this;
  }

  public F skipFollowing() {
    skipFollowing = true;
    return failed();
  }

  public F skipFollowingOnCondition(boolean condition) {
    if (condition) {
      return skipFollowing();
    } else {
      return (F) this;
    }
  }

  public F skipFollowingOnCondition(Predicate<F> condition) {
    return skipFollowingOnCondition(condition.test((F) this));
  }

  public F skipFollowingOnErrors() {
    if (errors.getErrorCount() > 0) {
      return skipFollowing();
    }
    return (F) this;
  }

  String getCode() {
    return StringFunc.concatWithDelim(fieldInfo.context, ".", fieldInfo.field);
  }

  Object[] getParameters(Map<String, Object> beans) {
    return new Object[] {
      ParamsFactory.init().addBean("name", "entity." + getCode()).addBeans(beans).getBeans()
    };
  }

  public static <T, F extends FieldValidator<T, F>> FieldValidatorBuilder<T, F> builder() {
    return new FieldValidatorBuilder();
  }

  public static class FieldValidatorBuilder<T, F extends FieldValidator<T, F>> {

    private FieldInfo<? extends T> fieldInfo;
    private Errors errors;
    private T value;
    private String nestedPath;

    public F start() {
      return (F) build();
    }

    public FieldValidatorBuilder<T, F> fieldInfo(FieldInfo<? extends T> fieldInfo) {
      this.fieldInfo = fieldInfo;
      return this;
    }

    public FieldValidatorBuilder<T, F> errors(Errors errors) {
      this.errors = errors;
      return this;
    }

    public FieldValidatorBuilder<T, F> value(T value) {
      this.value = value;
      return this;
    }

    public FieldValidatorBuilder<T, F> nested(String nestedPath) {
      this.nestedPath = nestedPath;
      return this;
    }

    public FieldValidator<T, F> build() {
      return new FieldValidator(this.fieldInfo, this.errors, this.value, nestedPath);
    }
  }

  public boolean isPassed() {
    return !skipFollowing && !failed;
  }

  @Builder
  @FieldDefaults(level = AccessLevel.PUBLIC)
  public static class ErrorInfo {

    @NonNull final String apiCode;
    final String defaultText;
    @With final Map<String, Object> parameters;

    public static ErrorInfoBuilder code(String apiCode) {
      return builder().apiCode(apiCode);
    }

    public static ErrorInfo of(String code, String defaultText) {
      return new ErrorInfo(code, defaultText, null);
    }
  }

  public F fieldError(String apiCode, String defaultText) {
    return fieldError(ErrorInfo.code(apiCode).defaultText(defaultText).build());
  }

  public F fieldError(boolean condition, String apiCode, String defaultText) {
    return fieldError(condition, ErrorInfo.code(apiCode).defaultText(defaultText).build());
  }

  public F fieldError(String apiCode, Map<String, Object> parameters, String defaultText) {
    return fieldError(
        ErrorInfo.code(apiCode).parameters(parameters).defaultText(defaultText).build());
  }

  public F fieldError(ErrorInfo fieldError) {
    return fieldError(true, fieldError);
  }

  public F fieldError(boolean condition, ErrorInfo fieldError) {
    if (!skipFollowing && condition) {
      if (nestedPath != null) {
        errors.pushNestedPath(nestedPath);
      }
      errors.rejectValue(
          fieldInfo.field,
          fieldError.apiCode,
          getParameters(fieldError.parameters),
          fieldError.defaultText);
      if (nestedPath != null) {
        errors.popNestedPath();
      }
      return failed();
    }
    return (F) this;
  }

  public F nonNull() {
    return validateNull();
  }

  /** Checks objects for nullability */
  // TODO add entity name -> to format full
  public F validateNull() {
    if (!skipFollowing) {
      if (value == null || value instanceof String && isBlank((String) value)) {
        fieldError(API_NOT_NULL);
        skipFollowing();
      }
    }
    return (F) this;
  }

  /**
   * Checks if object contains only allowed characters
   *
   * @param symbols String with acceptable symbols
   * @param symbolsDesc Human-readable description of symbols set
   */
  protected F validateSymbolSet(String symbols, String symbolsDesc) {
    if (!skipFollowing && value instanceof String && !containsOnly((String) value, symbols)) {
      fieldError(API_SYMBOLS_SET);
    }
    return (F) this;
  }

  public F maxLength(int maxLength) {
    if (!skipFollowing) {
      fieldError(
          nvlToString(value).length() > maxLength,
          API_LENGTH_TOO_LONG.withParameters(ImmutableMap.of("length", maxLength)));
    }
    return (F) this;
  }

  public F minLength(int minLength) {
    if (!skipFollowing) {
      fieldError(
          nvlToString(value).length() < minLength,
          API_LENGTH_TOO_SHORT.withParameters(ImmutableMap.of("length", minLength)));
    }
    return (F) this;
  }

  public F validateCustom(Function<F, Boolean> validator, Supplier<ErrorInfo> errorInfo) {
    if (!skipFollowing) {
      Boolean condition = validator.apply((F) this);
      if (nvl(condition, false)) {
        return fieldError(condition, errorInfo.get());
      }
    }
    return (F) this;
  }
}
