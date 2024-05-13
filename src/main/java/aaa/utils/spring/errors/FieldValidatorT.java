package aaa.utils.spring.errors;

public class FieldValidatorT<T> extends FieldValidator<T, FieldValidatorT<T>> {

  public static <T> FieldValidatorTBuilder<T> builderT() {
    return new FieldValidatorTBuilder<>();
  }

  public static class FieldValidatorTBuilder<T>
      extends FieldValidatorBuilder<T, FieldValidatorT<T>> {
    public FieldValidatorT build() {
      return new FieldValidatorT(super.build());
    }
  }

  public FieldValidatorT(FieldValidator<T, FieldValidatorT<T>> fieldValidator) {
    super(
        fieldValidator.fieldInfo,
        fieldValidator.errors,
        fieldValidator.value,
        fieldValidator.nestedPath);
  }
}
