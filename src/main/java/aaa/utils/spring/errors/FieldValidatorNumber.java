package aaa.utils.spring.errors;

import aaa.currency.CurrencyUtils;
import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class FieldValidatorNumber extends FieldValidator<Number, FieldValidatorNumber> {

  private static final FieldValidator.ErrorInfo API_UNDETERMINED_VALUE =
      ErrorInfo.code("api.undetermined_value")
          .defaultText(
              "Невозможно сохранить в поле \"$i18n.resolveCode($name)\" неопределённое значение")
          .build();
  private static final FieldValidator.ErrorInfo API_BEFORE_COMMA_TOO_LONG =
      ErrorInfo.code("api.before_comma_too_long")
          .defaultText(
              "Количество знаков до запятой поля \"$i18n.resolveCode($name)\""
                  + " должно быть не более $count")
          .build();
  private static final FieldValidator.ErrorInfo API_AFTER_COMMA_TOO_LONG =
      ErrorInfo.code("api.after_comma_too_long")
          .defaultText(
              "Количество знаков после запятой поля \"$i18n.resolveCode($name)\""
                  + " должно быть не более $count")
          .build();
  private static final FieldValidator.ErrorInfo API_TOO_BIG_DOUBLE =
      ErrorInfo.code("api.too_big_double")
          .defaultText(
              "Значение поля \"$i18n.resolveCode($name)\" не может быть больше или равно $doubleFormat.format($max)")
          .build();
  private static final FieldValidator.ErrorInfo API_TOO_BIG_DOUBLE_PRECISE =
      ErrorInfo.code("api.too_big_double.precise")
          .defaultText(
              "Значение поля \"$i18n.resolveCode($name)\" не может быть больше $doubleFormat.format($max)")
          .build();
  private static final FieldValidator.ErrorInfo API_TOO_SMALL_DOUBLE =
      ErrorInfo.code("api.too_small_double")
          .defaultText(
              "Значение поля \"$i18n.resolveCode($name)\" не может быть меньше или равно $doubleFormat.format($min)")
          .build();
  private static final FieldValidator.ErrorInfo API_TOO_SMALL_DOUBLE_PRECISE =
      ErrorInfo.code("api.too_small_double.precise")
          .defaultText(
              "Значение поля \"$i18n.resolveCode($name)\" не может быть меньше $doubleFormat.format($min)")
          .build();

  private BigDecimal bigDecimalValue;

  public static class FieldValidatorNumberBuilder
      extends FieldValidatorBuilder<Number, FieldValidatorNumber> {

    public FieldValidatorNumber build() {
      return new FieldValidatorNumber(super.build());
    }
  }

  public FieldValidatorNumber(FieldValidator<Number, FieldValidatorNumber> fieldValidator) {
    super(
        fieldValidator.fieldInfo,
        fieldValidator.errors,
        fieldValidator.value,
        fieldValidator.nestedPath);
    bigDecimalValue = value == null ? null : new BigDecimal(value.toString());
  }

  /** Validates Double values lengths (before comma) */
  protected FieldValidatorNumber integerPrecisionMax(Integer maxBeforeComma) {
    if (!skipFollowing && value != null) {
      if (determinate().isPassed()) {
        if (maxBeforeComma != null
            && CurrencyUtils.decimalPlacesBeforeComma(bigDecimalValue) > maxBeforeComma) {
          fieldError(
              API_BEFORE_COMMA_TOO_LONG.withParameters(ImmutableMap.of("count", maxBeforeComma)));
        }
      }
    }
    return this;
  }

  /** Validates Double values lengths (before comma) */
  public FieldValidatorNumber determinate() {
    if (!skipFollowing && value != null) {
      if (isUndetermined(value)) {
        fieldError(API_UNDETERMINED_VALUE);
        skipFollowing();
      }
    }
    return this;
  }

  /** Validates Double values lengths (before comma) */
  public FieldValidatorNumber fractionPrecisionMax(Integer maxAfterComma) {
    if (!skipFollowing && value != null) {
      if (determinate().isPassed()) {
        if (maxAfterComma != null
            && CurrencyUtils.decimalPlacesAfterComma(bigDecimalValue) > maxAfterComma) {
          fieldError(
              API_AFTER_COMMA_TOO_LONG.withParameters(ImmutableMap.of("count", maxAfterComma)));
        }
      }
    }
    return this;
  }

  public boolean isUndetermined(@NonNull Number value) {
    if (value instanceof Double) {
      return ((Double) value).isNaN() || ((Double) value).isInfinite();
    }
    if (value instanceof Float) {
      return ((Float) value).isNaN() || ((Float) value).isInfinite();
    }
    return false;
  }

  public FieldValidatorNumber min(BigDecimal min, ComparisonType type) {
    if (!skipFollowing && value != null) {
      boolean precise = type == ComparisonType.INCLUDE_BOUNDARIES;
      fieldError(
          !precise && bigDecimalValue.compareTo(min) <= 0,
          API_TOO_SMALL_DOUBLE.withParameters(ImmutableMap.of("min", min)));
      fieldError(
          precise && bigDecimalValue.compareTo(min) < 0,
          API_TOO_SMALL_DOUBLE_PRECISE.withParameters(ImmutableMap.of("min", min)));
    }
    return this;
  }

  public FieldValidatorNumber max(BigDecimal max, ComparisonType type) {
    if (!skipFollowing && value != null) {
      boolean precise = type == ComparisonType.INCLUDE_BOUNDARIES;
      fieldError(
          !precise && bigDecimalValue.compareTo(max) >= 0,
          API_TOO_BIG_DOUBLE.withParameters(ImmutableMap.of("max", max)));
      fieldError(
          precise && bigDecimalValue.compareTo(max) > 0,
          API_TOO_BIG_DOUBLE_PRECISE.withParameters(ImmutableMap.of("max", max)));
    }
    return this;
  }

  public FieldValidatorNumber ge(BigDecimal min) {
    return min(min, ComparisonType.INCLUDE_BOUNDARIES);
  }

  public FieldValidatorNumber gt(BigDecimal min) {
    return min(min, ComparisonType.OMIT_BOUNDARIES);
  }

  public FieldValidatorNumber le(BigDecimal max) {
    return max(max, ComparisonType.INCLUDE_BOUNDARIES);
  }

  public FieldValidatorNumber lt(BigDecimal max) {
    return max(max, ComparisonType.OMIT_BOUNDARIES);
  }
}
