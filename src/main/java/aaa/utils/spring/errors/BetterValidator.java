package aaa.utils.spring.errors;

import static aaa.lang.reflection.IntrospectionUtils.asGetter;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import aaa.lang.reflection.ReflectionUtils;
import aaa.lang.reflection.getter.GetterPropertyResolver;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class BetterValidator<T> extends BaseValidator<T> {

  public final Class<T> entityClass;
  private final String entityClassName;
  private final GetterPropertyResolver<T> getterPropertyResolver;

  @AllArgsConstructor
  @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
  protected class Context {
    T entity;
    Errors errors;

    Map<String, Object> cache = new HashMap<>();

    private <V> FieldInfo<V> fieldInfo(String fieldName) {
      return FieldInfo.<V>builder().context(entityClassName).field(fieldName).build();
    }

    public <V> FieldValidatorT<V> forPojo(Function<T, V> getter, String fieldName) {
      return (FieldValidatorT<V>)
          cache.computeIfAbsent(
              fieldName,
              f ->
                  FieldValidatorT.<V>builderT()
                      .errors(errors)
                      .fieldInfo(fieldInfo(fieldName))
                      .value(getter.apply(entity))
                      .start());
    }

    public FieldValidatorString forString(Function<T, String> getter, String fieldName) {
      return (FieldValidatorString)
          cache.computeIfAbsent(
              fieldName,
              f ->
                  FieldValidatorString.builder()
                      .errors(errors)
                      .fieldInfo(fieldInfo(fieldName))
                      .value(getter.apply(entity))
                      .start());
    }

    public FieldValidatorDate forDate(Function<T, Date> getter, String fieldName) {
      return (FieldValidatorDate)
          cache.computeIfAbsent(
              fieldName,
              f ->
                  FieldValidatorDate.builder()
                      .errors(errors)
                      .fieldInfo(fieldInfo(fieldName))
                      .value(getter.apply(entity))
                      .start());
    }

    public FieldValidatorTemporal forTemporal(
        Function<T, ? extends Temporal> getter, String fieldName) {
      return (FieldValidatorTemporal)
          cache.computeIfAbsent(
              fieldName,
              f ->
                  FieldValidatorTemporal.builder()
                      .errors(errors)
                      .fieldInfo(fieldInfo(fieldName))
                      .value(getter.apply(entity))
                      .start());
    }

    public FieldValidatorLong forLong(Function<T, Long> getter, String fieldName) {
      return (FieldValidatorLong)
          cache.computeIfAbsent(
              fieldName,
              f ->
                  FieldValidatorLong.builder()
                      .errors(errors)
                      .fieldInfo(fieldInfo(fieldName))
                      .value(getter.apply(entity))
                      .start());
    }

    public <V extends Number> FieldValidatorNumber forNumber(
        Function<T, V> getter, String fieldName) {
      return (FieldValidatorNumber)
          cache.computeIfAbsent(
              fieldName,
              f ->
                  FieldValidatorNumber.builder()
                      .errors(errors)
                      .fieldInfo(fieldInfo(fieldName))
                      .value(getter.apply(entity))
                      .start());
    }

    public <V> void subValidator(Validator validator, Function<T, V> getter, String fieldName) {
      if (validator == null || getter == null || fieldName == null) {
        return;
      }
      ofNullable(getter.apply(entity))
          .ifPresent(
              fieldValue -> {
                try {
                  errors.pushNestedPath(fieldName);
                  validator.validate(fieldValue, errors);
                } finally {
                  errors.popNestedPath();
                }
              });
    }

    public <V> FieldValidatorT<V> forPojo(SingularAttribute<T, V> attribute) {
      return forPojo(asGetter(attribute), attribute.getName());
    }

    public FieldValidatorString forString(SingularAttribute<T, String> attribute) {
      return forString(asGetter(attribute), attribute.getName());
    }

    public FieldValidatorDate forDate(SingularAttribute<T, Date> attribute) {
      return forDate(asGetter(attribute), attribute.getName());
    }

    public FieldValidatorTemporal forTemporal(SingularAttribute<T, ? extends Temporal> attribute) {
      return forTemporal(asGetter(attribute), attribute.getName());
    }

    public FieldValidatorLong forLong(SingularAttribute<T, Long> attribute) {
      return forLong(asGetter(attribute), attribute.getName());
    }

    public <V extends Number> FieldValidatorNumber forNumber(SingularAttribute<T, V> attribute) {
      return forNumber(asGetter(attribute), attribute.getName());
    }

    public <V> void subValidator(Validator validator, SingularAttribute<T, V> attribute) {
      subValidator(validator, asGetter(attribute), attribute.getName());
    }

    public <V> FieldValidatorT<V> forPojo(Function<T, V> getter) {
      return forPojo(getter, getterPropertyResolver.resolveName(getter));
    }

    public FieldValidatorString forString(Function<T, String> getter) {
      return forString(getter, getterPropertyResolver.resolveName(getter));
    }

    public FieldValidatorDate forDate(Function<T, Date> getter) {
      return forDate(getter, getterPropertyResolver.resolveName(getter));
    }

    public FieldValidatorTemporal forTemporal(Function<T, ? extends Temporal> getter) {
      return forTemporal(getter, getterPropertyResolver.resolveName(getter));
    }

    public FieldValidatorLong forLong(Function<T, Long> getter) {
      return forLong(getter, getterPropertyResolver.resolveName(getter));
    }

    public <V extends Number> FieldValidatorNumber forNumber(Function<T, V> getter) {
      return forNumber(getter, getterPropertyResolver.resolveName(getter));
    }

    public <V> void subValidator(Validator validator, Function<T, V> getter) {
      subValidator(validator, getter, getterPropertyResolver.resolveName(getter));
    }

    public <V> void subValidator(Validator validator, PluralAttribute<T, ?, V> attribute) {
      if (validator == null || attribute == null) {
        return;
      }
      ofNullable(ReflectionUtils.<T, List<V>>asGetter(attribute.getJavaMember()).apply(entity))
          .ifPresent(
              list -> {
                for (ListIterator<V> iterator = list.listIterator(); iterator.hasNext(); ) {
                  subValidator(
                      validator,
                      v -> iterator.next(),
                      attribute.getName() + "[" + iterator.nextIndex() + "]");
                }
              });
    }

    public <V> void subValidator(
        SingularAttribute<T, V> attribute, Consumer<BetterValidator<V>.Context> consumer) {
      if (consumer == null || attribute == null) {
        return;
      }
      ofNullable(asGetter(attribute).apply(entity))
          .ifPresent(
              fieldValue -> {
                try {
                  errors.pushNestedPath(attribute.getName());
                  new BetterValidator<>(attribute.getJavaType()) {
                    @Override
                    public void validate(Context context) {
                      consumer.accept(context);
                    }
                  }.validate(fieldValue, errors);
                } finally {
                  errors.popNestedPath();
                }
              });
    }

    public void run(Consumer<Context> consumer) {
      consumer.accept(this);
    }
  }

  public boolean supports(Class clazz) {
    return entityClass.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(asContext(target, errors));
  }

  public abstract void validate(Context context);

  protected Context asContext(Object target, Errors errors) {
    return new Context((T) target, errors);
  }

  public BetterValidator() {
    Type[] actualTypeArguments =
        ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
    entityClass = (Class<T>) actualTypeArguments[0];
    entityClassName = uncapitalize(entityClass.getSimpleName());
    getterPropertyResolver = ReflectionUtils.makeGetterPropertyResolver(entityClass);
  }

  private BetterValidator(Class<T> clazz) {
    entityClass = clazz;
    entityClassName = uncapitalize(entityClass.getSimpleName());
    getterPropertyResolver = ReflectionUtils.makeGetterPropertyResolver(entityClass);
  }
}
