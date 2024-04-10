package aaa.utils.spring.integration.jpa;

import java.util.Objects;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
public class AbstractPOJOUtils {

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
