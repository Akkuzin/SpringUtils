package aaa.utils.spring.errors;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
          (value instanceof LocalDate localDate
                  ? localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
                  : (value instanceof LocalDateTime localDateTime
                      ? localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                      : Instant.from(value)))
              .isAfter(Instant.now()),
          ErrorInfo.builder()
              .apiCode("api.noFutureDateAllowed")
              .defaultText(
                  "В поле \"$i18n.resolveCode($name)\" не может быть указана дата из будущего")
              .build());
    }
    return this;
  }
}
