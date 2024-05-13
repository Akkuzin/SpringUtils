package aaa.utils.spring.pojo;

import static aaa.lambda.LambdaUtils.sneakyThrowsSupplier;
import static aaa.utils.spring.errors.IntrospectionUtils.asGetter;
import static aaa.utils.spring.errors.IntrospectionUtils.asSetter;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import aaa.lang.reflection.ReflectionUtils;
import aaa.lang.reflection.getter.GetterPropertyResolver;
import aaa.nvl.Nvl;
import jakarta.persistence.metamodel.SingularAttribute;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.SneakyThrows;
import lombok.With;
import lombok.experimental.FieldDefaults;

public class PojoUtils {

  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  public static class SummatorBuilder<T> {
    Class<T> clazz;
    GetterPropertyResolver<T> resolver;
    List<FieldInfo<T, ?>> info = new ArrayList<>();

    private SummatorBuilder(Class<T> clazz) {
      this.clazz = clazz;
      this.resolver = ReflectionUtils.makeGetterPropertyResolver(clazz);
    }

    public static <T> SummatorBuilder<T> forClass(Class<T> clazz) {
      return new SummatorBuilder<T>(clazz);
    }

    public SummatorBuilder<T> add(Function<T, ?> getter) {
      return add(getter, identity());
    }

    public <V> SummatorBuilder<T> add(
        Function<T, V> getter, Function<FieldInfo<T, V>, FieldInfo<T, V>> custom) {
      return add(FieldInfo.of(resolver, clazz, getter), custom);
    }

    public SummatorBuilder<T> add(SingularAttribute<T, ?> attribute) {
      return add(attribute, identity());
    }

    public <V> SummatorBuilder<T> add(
        SingularAttribute<T, V> attribute, Function<FieldInfo<T, V>, FieldInfo<T, V>> custom) {
      return add(FieldInfo.of(attribute), custom);
    }

    public SummatorBuilder<T> add(FieldInfo<T, ?> fieldInfo) {
      return add(fieldInfo, identity());
    }

    public <V> SummatorBuilder<T> add(
        FieldInfo<T, V> fieldInfo, Function<FieldInfo<T, V>, FieldInfo<T, V>> custom) {
      info.add(custom.apply(fieldInfo));
      return this;
    }

    @SneakyThrows
    public BinaryOperator<T> build() {
      Supplier<T> constructor = sneakyThrowsSupplier(clazz.getConstructor()::newInstance);
      return (v1, v2) -> {
        T result = constructor.get();
        for (FieldInfo fieldInfo : info) {
          fieldInfo.setter.accept(result, fieldInfo.reduce(v1, v2));
        }
        return result;
      };
    }
  }

  @AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
  // @FieldDefaults(makeFinal = true)
  @Builder(toBuilder = true)
  public static class FieldInfo<T, V> {
    @With Function<T, V> getter;
    @With BiConsumer<T, V> setter;
    Class<V> type;
    @With @Default Predicate<T> preFilter = Objects::nonNull;
    @With @Default Predicate<V> filter = Objects::nonNull;
    @With BinaryOperator<V> operation;
    @With V defaultValue;

    public FieldInfo<T, V> withPeekFirstOperation() {
      return this.withOperation((v1, v2) -> v1);
    }

    V reduce(T v1, T v2) {
      V r1 = preFilter.test(v1) ? getter.apply(v1) : null;
      V r2 = preFilter.test(v2) ? getter.apply(v2) : null;
      r1 = filter.test(r1) ? r1 : null;
      r2 = filter.test(r2) ? r2 : null;
      return ofNullable(
              r1 == null ? r2 == null ? null : r2 : r2 == null ? r1 : operation.apply(r1, r2))
          .orElse(defaultValue);
    }

    private static <T, V> FieldInfo<T, V> of(SingularAttribute<T, V> attribute) {
      return FieldInfo.builder()
          .getter((Function) asGetter(attribute))
          .setter(asSetter(attribute))
          .type(attribute.getJavaType())
          .operation(reducerForType(attribute.getJavaType()))
          .defaultValue(defaultValue(attribute.getJavaType()))
          .build();
    }

    private static <T, V> FieldInfo<T, V> of(
        GetterPropertyResolver<T> resolver, Class<T> clazz, Function<T, V> getter) {
      Class<V> type = resolver.resolveType(getter);
      return FieldInfo.<T, V>builder()
          .getter(getter)
          .setter(asSetter(clazz, resolver.resolveName(getter), type))
          .type(type)
          .operation(reducerForType(type))
          .defaultValue(defaultValue(type))
          .build();
    }
  }

  static BinaryOperator reducerForType(Class clazz) {
    if (BigDecimal.class.isAssignableFrom(clazz)) {
      return (BinaryOperator<BigDecimal>) BigDecimal::add;
    }
    if (Long.class.isAssignableFrom(clazz)) {
      return (BinaryOperator<Long>) Long::sum;
    }
    if (Integer.class.isAssignableFrom(clazz)) {
      return (BinaryOperator<Integer>) Integer::sum;
    }
    if (String.class.isAssignableFrom(clazz)) {
      return (BinaryOperator<String>) (s1, s2) -> isNotBlank(s1) ? s1 : isNotBlank(s2) ? s2 : null;
    }
    if (boolean.class == clazz) {
      return (BinaryOperator<Boolean>) Boolean::logicalOr;
    }
    return Nvl::nvl;
  }

  public static <V> V defaultValue(Class<V> type) {
    if (BigDecimal.class.isAssignableFrom(type)) {
      return (V) BigDecimal.ZERO;
    } else if (Long.class.isAssignableFrom(type)) {
      return (V) (Long) 0L;
    } else if (Integer.class.isAssignableFrom(type)) {
      return (V) (Integer) 0;
    } else {
      return null;
    }
  }
}
