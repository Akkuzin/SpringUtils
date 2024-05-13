package aaa.utils.spring.errors;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;

@Builder
public class FieldValidatorLong extends FieldValidator<Long, FieldValidatorLong> {

  private static final FieldValidator.ErrorInfo API_TOO_SMALL =
      ErrorInfo.code("api.too_small")
          .defaultText(
              "Значение поля \"$i18n.resolveCode($name)\" не может быть меньше или равно $min")
          .build();
  private static final FieldValidator.ErrorInfo API_TOO_SMALL_PRECISE =
      ErrorInfo.code("api.too_small.precise")
          .defaultText("Значение поля \"$i18n.resolveCode($name)\" не может быть меньше $min")
          .build();
  private static final FieldValidator.ErrorInfo API_TOO_BIG =
      ErrorInfo.code("api.too_big")
          .defaultText(
              "Значение поля \"$i18n.resolveCode($name)\" не может быть больше или равно $max")
          .build();
  private static final FieldValidator.ErrorInfo API_TOO_BIG_PRECISE =
      ErrorInfo.code("api.too_big" + ".precise")
          .defaultText("Значение поля \"$i18n.resolveCode($name)\" не может быть больше $max")
          .build();

  public static class FieldValidatorLongBuilder
      extends FieldValidatorBuilder<Long, FieldValidatorLong> {
    public FieldValidatorLong build() {
      return new FieldValidatorLong(super.build());
    }
  }

  public FieldValidatorLong(FieldValidator<Long, FieldValidatorLong> fieldValidator) {
    super(
        fieldValidator.fieldInfo,
        fieldValidator.errors,
        fieldValidator.value,
        fieldValidator.nestedPath);
  }

  public FieldValidatorLong max(long max, ComparisonType type) {
    if (!skipFollowing && value != null) {
      boolean precise = type == ComparisonType.INCLUDE_BOUNDARIES;
      fieldError(
          precise && value > max, API_TOO_BIG_PRECISE.withParameters(ImmutableMap.of("max", max)));
      fieldError(!precise && value >= max, API_TOO_BIG.withParameters(ImmutableMap.of("max", max)));
    }
    return this;
  }

  public FieldValidatorLong min(long min, ComparisonType type) {
    if (!skipFollowing && value != null) {
      boolean precise = type == ComparisonType.INCLUDE_BOUNDARIES;
      fieldError(
          precise && value < min,
          API_TOO_SMALL_PRECISE.withParameters(ImmutableMap.of("min", min)));
      fieldError(
          !precise && value <= min, API_TOO_SMALL.withParameters(ImmutableMap.of("min", min)));
    }
    return this;
  }

  public FieldValidatorLong ge(long min) {
    return min(min, ComparisonType.INCLUDE_BOUNDARIES);
  }

  public FieldValidatorLong gt(long min) {
    return min(min, ComparisonType.OMIT_BOUNDARIES);
  }

  public FieldValidatorLong le(long max) {
    return max(max, ComparisonType.INCLUDE_BOUNDARIES);
  }

  public FieldValidatorLong lt(long max) {
    return max(max, ComparisonType.OMIT_BOUNDARIES);
  }
}
