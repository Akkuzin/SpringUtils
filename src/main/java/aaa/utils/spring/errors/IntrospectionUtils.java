package aaa.utils.spring.errors;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.capitalize;

import aaa.lang.reflection.ReflectionUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.persistence.metamodel.Attribute;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.SneakyThrows;

public class IntrospectionUtils {

  private static final Cache<Attribute, Function> GETTERS = CacheBuilder.newBuilder().build();

  @SneakyThrows
  public static <T, V> Function<T, V> asGetter(Attribute<T, V> attribute) {
    return GETTERS.get(attribute, () -> ReflectionUtils.asGetter(attribute.getJavaMember()));
  }

  private static final Cache<Object, BiConsumer> SETTERS = CacheBuilder.newBuilder().build();

  @SneakyThrows
  public static <T, V> BiConsumer<T, V> asSetter(Attribute<T, V> attribute) {
    return asSetter(
        attribute.getDeclaringType().getJavaType(), attribute.getName(), attribute.getJavaType());
  }

  @SneakyThrows
  public static <T, V> BiConsumer<T, V> asSetter(
      Class<T> clazz, String propertyName, Class<V> propertyType) {
    return SETTERS.get(
        asList(clazz, propertyName),
        () ->
            ReflectionUtils.asSetter(
                clazz.getMethod("set" + capitalize(propertyName), propertyType)));
  }
}
