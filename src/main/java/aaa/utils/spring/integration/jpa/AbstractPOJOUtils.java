package aaa.utils.spring.integration.jpa;

import java.util.Objects;
import java.util.Optional;
import org.hibernate.proxy.HibernateProxy;

public class AbstractPOJOUtils {

  public static <T extends IAbstractPOJO> boolean pojoEquals(
      Optional<T> first, Optional<T> second) {
    return Objects.equals(first, second)
        || (first.isPresent() && second.isPresent() && pojoEquals(first.get(), second.get()));
  }

  public static <T extends IAbstractPOJO> boolean pojoEquals(T first, T second) {
    return first == second
        || (first != null
            && second != null
            && Objects.equals(getPojoClass(first), getPojoClass(second))
            && first.getId() != null
            && Objects.equals(first.getId(), second.getId()));
  }

  public static <T> Class<T> getPojoClass(T proxy) {
    return (Class<T>)
        (proxy instanceof HibernateProxy hibernateProxy
            ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass()
            : proxy.getClass());
  }
}
