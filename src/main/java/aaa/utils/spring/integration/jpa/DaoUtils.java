package aaa.utils.spring.integration.jpa;

import static aaa.lambda.LambdaUtils.caster;
import static aaa.lang.reflection.ReflectionUtils.makeGetterPropertyResolver;
import static aaa.utils.spring.integration.jpa.AbstractPOJOUtils.getPojoClass;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.strip;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.apache.commons.lang3.StringUtils.substring;

import aaa.lang.reflection.ReflectionUtils;
import aaa.lang.reflection.getter.GetterPropertyResolver;
import aaa.utils.spring.errors.IntrospectionUtils;
import aaa.utils.spring.pojo.PojoUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;

@UtilityClass
public class DaoUtils {

  public static <T> T initialize(T entity) throws HibernateException {
    Hibernate.initialize(entity);
    return entity;
  }

  public static <T> T unproxy(T entity) throws HibernateException {
    Hibernate.initialize(entity);
    if (entity instanceof HibernateProxy) {
      entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
    }
    return entity;
  }

  private static final Cache<Object, Function> GETTERS = CacheBuilder.newBuilder().build();

  @SneakyThrows
  public static Function asGetter(Class clazz, String field) {
    return GETTERS.get(
        asList(clazz, field),
        () -> ReflectionUtils.asGetter(clazz.getMethod("get" + capitalize(field))));
  }

  private static final Cache<Object, BiConsumer> SETTERS = CacheBuilder.newBuilder().build();

  @SneakyThrows
  public static BiConsumer asSetter(Class clazz, String field) {
    return SETTERS.get(
        asList(clazz, field),
        () ->
            ReflectionUtils.asSetter(
                clazz.getMethod("set" + capitalize(field), asType(clazz, field))));
  }

  private static final Cache<Object, GetterPropertyResolver> RESOLVERS =
      CacheBuilder.newBuilder().build();
  private static final Cache<Object, Class> FIELD_TYPES = CacheBuilder.newBuilder().build();

  @SneakyThrows
  public static Class asType(Class clazz, String field) {
    return FIELD_TYPES.get(
        asList(clazz, field),
        () ->
            RESOLVERS
                .get(clazz, () -> makeGetterPropertyResolver(clazz))
                .resolveType(asGetter(clazz, field)));
  }

  public static String limitLength(int length, String value) {
    return substring(strip(value), 0, length);
  }

  public static <T> void limitLength(T value, SingularAttribute<T, String> attribute, int length) {
    if (value == null) {
      return;
    }
    IntrospectionUtils.asSetter(attribute)
        .accept(
            value,
            substring(strip(IntrospectionUtils.asGetter(attribute).apply(value)), 0, length));
  }

  public static <T> void limitLength(
      T value, Map<SingularAttribute<T, String>, Integer> attributesLength) {
    attributesLength.forEach((attribute, length) -> limitLength(value, attribute, length));
  }

  public static <T, V> T copyData(T source, T target, Attribute<T, V> attribute) {
    IntrospectionUtils.asSetter(attribute)
        .accept(target, IntrospectionUtils.asGetter(attribute).apply(source));
    return target;
  }

  @AllArgsConstructor(staticName = "on")
  public static class DataCopier<T> {
    T source;
    T destination;

    public void copyData(SingularAttribute<T, ?>... attributes) {
      DaoUtils.copyData(source, destination, attributes);
    }
  }

  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  public static class Copier<T> {

    @NonNull T fromSource;
    @NonNull T toTarget;

    public T copyFields(Attribute<T, ?>... fields) {
      return copyData(fromSource, toTarget, fields);
    }

    public static class CopierBuilder<T> {
      public T copyFields(Attribute<T, ?>... fields) {
        return build().copyFields(fields);
      }
    }
  }

  public static <TT> CopierFrom<TT> fromSource(TT source) {
    return new CopierFrom<>(source);
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class CopierFrom<T> {

    T source;

    public Copier<T> toTarget(T target) {
      return new Copier<>(source, target);
    }

    @SneakyThrows
    public Copier<T> toNew() {
      return new Copier<>(source, getPojoClass(source).getConstructor().newInstance());
    }
  }

  public static <T> T copyData(T source, T target, Attribute<T, ?>... attributes) {
    if (source != null) {
      Stream.of(attributes).forEach(attribute -> copyData(source, target, attribute));
    }
    return target;
  }

  public static <T> T stripFields(T source, Collection<SingularAttribute<T, String>> attributes) {
    attributes.forEach(
        attribute ->
            IntrospectionUtils.asSetter(attribute)
                .accept(source, stripToNull(IntrospectionUtils.asGetter(attribute).apply(source))));
    return source;
  }

  public static <T> T stripFields(T source, SingularAttribute<T, String>... attributes) {
    return stripFields(source, asList(attributes));
  }

  public static <T> T nvlZero(T source, String... fields) {
    if (source != null) {
      Class<T> clazz = getPojoClass(source);
      for (String field : fields) {
        if (asGetter(clazz, field).apply(source) == null) {
          Class<?> type = asType(clazz, field);
          if (Number.class.isAssignableFrom(type)) {
            asSetter(clazz, field).accept(source, PojoUtils.defaultValue(type));
          }
        }
      }
    }
    return source;
  }

  public static <T> T nvlZero(T source, SingularAttribute<T, ?>... attributes) {
    if (source != null) {
      for (SingularAttribute<T, ?> attribute : attributes) {
        if (IntrospectionUtils.asGetter(attribute).apply(source) == null) {
          Class<?> type = attribute.getJavaType();
          if (Number.class.isAssignableFrom(type)) {
            ((BiConsumer) IntrospectionUtils.asSetter(attribute))
                .accept(source, PojoUtils.defaultValue(type));
          }
        }
      }
    }
    return source;
  }

  public static <T extends IAbstractPOJO> void cacheInvalidate(
      SessionFactory sessionFactory, Class<T> clazz, Collection<?> entities) {
    org.hibernate.Cache cache = sessionFactory.getCache();
    if (entities == null || entities.isEmpty()) {
      cache.evictEntityData(clazz);
    } else {
      entities.stream()
          .filter(Objects::nonNull)
          .map(caster(clazz))
          .filter(Objects::nonNull)
          .map(IAbstractPOJO::getId)
          .filter(Objects::nonNull)
          .distinct()
          .forEach(id -> cache.evictEntityData(clazz, id));
    }
  }

  public static String getTable(Class<?> clazz) {
    return clazz.getAnnotation(Table.class).name();
  }

  @SneakyThrows
  public static <T> T extractView(T data, SingularAttribute<T, ?>... attributes) {
    return fromSource(data).toNew().copyFields(attributes);
  }
}
