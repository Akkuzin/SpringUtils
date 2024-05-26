package aaa.utils.spring.integration.jpa;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.CollectionAttribute;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import jakarta.persistence.metamodel.Type;
import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.SneakyThrows;

@StaticMetamodel(Data.class)
@Generated("org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
public abstract class Data_ {

  public static final String NAME = "name";
  public static final String SUM = "sum";
  public static final String COUNT = "count";
  public static final String DATE = "date";

  public static volatile SingularAttribute<Data, String> name = make(String.class, NAME);
  public static volatile SingularAttribute<Data, BigDecimal> sum = make(BigDecimal.class, SUM);
  public static volatile SingularAttribute<Data, Long> count = make(Long.class, COUNT);
  public static volatile SingularAttribute<Data, LocalDateTime> date =
      make(LocalDateTime.class, DATE);

  public static <Y> SingularAttribute<Data, Y> make(Class<Y> clazz, String name) {
    return new SingularAttribute<>() {
      @Override
      public BindableType getBindableType() {
        return null;
      }

      @Override
      public Class<Y> getBindableJavaType() {
        return null;
      }

      @Override
      public String getName() {
        return name;
      }

      @Override
      public PersistentAttributeType getPersistentAttributeType() {
        return null;
      }

      @Override
      public ManagedType<Data> getDeclaringType() {
        return new ManagedType<>() {
          @Override
          public Set<Attribute<? super Data, ?>> getAttributes() {
            return Set.of();
          }

          @Override
          public Set<Attribute<Data, ?>> getDeclaredAttributes() {
            return Set.of();
          }

          @Override
          public <Y> SingularAttribute<? super Data, Y> getSingularAttribute(
              String name, Class<Y> type) {
            return null;
          }

          @Override
          public <Y> SingularAttribute<Data, Y> getDeclaredSingularAttribute(
              String name, Class<Y> type) {
            return null;
          }

          @Override
          public Set<SingularAttribute<? super Data, ?>> getSingularAttributes() {
            return Set.of();
          }

          @Override
          public Set<SingularAttribute<Data, ?>> getDeclaredSingularAttributes() {
            return Set.of();
          }

          @Override
          public <E> CollectionAttribute<? super Data, E> getCollection(
              String name, Class<E> elementType) {
            return null;
          }

          @Override
          public <E> CollectionAttribute<Data, E> getDeclaredCollection(
              String name, Class<E> elementType) {
            return null;
          }

          @Override
          public <E> SetAttribute<? super Data, E> getSet(String name, Class<E> elementType) {
            return null;
          }

          @Override
          public <E> SetAttribute<Data, E> getDeclaredSet(String name, Class<E> elementType) {
            return null;
          }

          @Override
          public <E> ListAttribute<? super Data, E> getList(String name, Class<E> elementType) {
            return null;
          }

          @Override
          public <E> ListAttribute<Data, E> getDeclaredList(String name, Class<E> elementType) {
            return null;
          }

          @Override
          public <K, V> MapAttribute<? super Data, K, V> getMap(
              String name, Class<K> keyType, Class<V> valueType) {
            return null;
          }

          @Override
          public <K, V> MapAttribute<Data, K, V> getDeclaredMap(
              String name, Class<K> keyType, Class<V> valueType) {
            return null;
          }

          @Override
          public Set<PluralAttribute<? super Data, ?, ?>> getPluralAttributes() {
            return Set.of();
          }

          @Override
          public Set<PluralAttribute<Data, ?, ?>> getDeclaredPluralAttributes() {
            return Set.of();
          }

          @Override
          public Attribute<? super Data, ?> getAttribute(String name) {
            return null;
          }

          @Override
          public Attribute<Data, ?> getDeclaredAttribute(String name) {
            return null;
          }

          @Override
          public SingularAttribute<? super Data, ?> getSingularAttribute(String name) {
            return null;
          }

          @Override
          public SingularAttribute<Data, ?> getDeclaredSingularAttribute(String name) {
            return null;
          }

          @Override
          public CollectionAttribute<? super Data, ?> getCollection(String name) {
            return null;
          }

          @Override
          public CollectionAttribute<Data, ?> getDeclaredCollection(String name) {
            return null;
          }

          @Override
          public SetAttribute<? super Data, ?> getSet(String name) {
            return null;
          }

          @Override
          public SetAttribute<Data, ?> getDeclaredSet(String name) {
            return null;
          }

          @Override
          public ListAttribute<? super Data, ?> getList(String name) {
            return null;
          }

          @Override
          public ListAttribute<Data, ?> getDeclaredList(String name) {
            return null;
          }

          @Override
          public MapAttribute<? super Data, ?, ?> getMap(String name) {
            return null;
          }

          @Override
          public MapAttribute<Data, ?, ?> getDeclaredMap(String name) {
            return null;
          }

          @Override
          public PersistenceType getPersistenceType() {
            return null;
          }

          @Override
          public Class<Data> getJavaType() {
            return null;
          }
        };
      }

      @Override
      public Class<Y> getJavaType() {
        return clazz;
      }

      @Override
      @SneakyThrows
      public Member getJavaMember() {
        return Data.class.getField(name);
      }

      @Override
      public boolean isAssociation() {
        return false;
      }

      @Override
      public boolean isCollection() {
        return false;
      }

      @Override
      public boolean isId() {
        return false;
      }

      @Override
      public boolean isVersion() {
        return false;
      }

      @Override
      public boolean isOptional() {
        return false;
      }

      @Override
      public Type<Y> getType() {
        return null;
      }
    };
  }
}
