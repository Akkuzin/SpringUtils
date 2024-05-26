package aaa.utils.spring.errors;

import static aaa.i18n.ru.TransliterateUtils.isMix;
import static com.google.common.base.Strings.nullToEmpty;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;

@Builder
public class FieldValidatorString extends FieldValidator<String, FieldValidatorString> {

  public static class FieldValidatorStringBuilder
      extends FieldValidatorBuilder<String, FieldValidatorString> {
    public FieldValidatorString build() {
      return new FieldValidatorString(super.build());
    }
  }

  public FieldValidatorString(FieldValidator<String, FieldValidatorString> fieldValidator) {
    super(
        fieldValidator.fieldInfo,
        fieldValidator.errors,
        fieldValidator.value,
        fieldValidator.nestedPath);
  }

  /** Validates field value match with custom regular expression */
  protected FieldValidatorString validateRegexp(String regexp, String regexpName) {
    if (!skipFollowing) {
      if (value == null || !value.matches(regexp)) {
        fieldError(
            "api.format",
            ImmutableMap.of("desc", nullToEmpty(regexpName)),
            "Значение поля \"$i18n.resolveCode($name)\" не соответствует допустимому формату ($desc)");
      }
    }
    return this;
  }

  /** Проверка перемешанных русских и латинских букв */
  protected FieldValidatorString noMix() {
    if (!skipFollowing) {
      if (isMix(value)) {
        fieldError(
            "api.string.character_mix",
            "Поле \"$i18n.resolveCode($name)\" не должно содержать смесь русских и латинских символов");
      }
    }
    return this;
  }
}
