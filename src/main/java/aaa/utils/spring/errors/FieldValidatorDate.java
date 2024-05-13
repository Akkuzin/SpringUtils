package aaa.utils.spring.errors;

import java.util.Date;
import lombok.Builder;

@Builder
public class FieldValidatorDate extends FieldValidator<Date, FieldValidatorDate> {

  public static class FieldValidatorDateBuilder
      extends FieldValidatorBuilder<Date, FieldValidatorDate> {
    public FieldValidatorDate build() {
      return new FieldValidatorDate(super.build());
    }
  }

  public FieldValidatorDate(FieldValidator<Date, FieldValidatorDate> fieldValidator) {
    super(
        fieldValidator.fieldInfo,
        fieldValidator.errors,
        fieldValidator.value,
        fieldValidator.nestedPath);
  }

  public FieldValidatorDate noFuture() {
    if (!skipFollowing && value != null) {
      fieldError(
          value.after(new Date()),
          ErrorInfo.builder()
              .apiCode("api.noFutureDateAllowed")
              .defaultText(
                  "В поле \"$i18n.resolveCode($name)\" не может быть указана дата из будущего")
              .build());
    }
    return this;
  }
}
