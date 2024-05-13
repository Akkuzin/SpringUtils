package aaa.utils.spring.errors;

import java.time.Instant;
import java.time.temporal.Temporal;
import lombok.Builder;

@Builder
public class FieldValidatorTemporal extends FieldValidator<Temporal, FieldValidatorTemporal> {

  public static class FieldValidatorTemporalBuilder
      extends FieldValidatorBuilder<Temporal, FieldValidatorTemporal> {
    public FieldValidatorTemporal build() {
      return new FieldValidatorTemporal(super.build());
    }
  }

  public FieldValidatorTemporal(FieldValidator<Temporal, FieldValidatorTemporal> fieldValidator) {
    super(
        fieldValidator.fieldInfo,
        fieldValidator.errors,
        fieldValidator.value,
        fieldValidator.nestedPath);
  }

  public FieldValidatorTemporal noFuture() {
    if (!skipFollowing && value != null) {
      fieldError(
          Instant.from(value).isAfter(Instant.now()),
          ErrorInfo.builder()
              .apiCode("api.noFutureDateAllowed")
              .defaultText(
                  "В поле \"$i18n.resolveCode($name)\" не может быть указана дата из будущего")
              .build());
    }
    return this;
  }
}
