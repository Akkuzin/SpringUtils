package aaa.utils.spring.errors;

import static aaa.utils.spring.integration.jpa.AbstractPOJOUtils.getPojoClass;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor(staticName = "init")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParamsFactory {

  Map<String, Object> beans = new HashMap<>();

  public ParamsFactory addBean(String name, Object object) {
    beans.put(name, object);
    return this;
  }

  public ParamsFactory addBeans(Map<String, Object> newBeans) {
    if (newBeans != null) {
      beans.putAll(newBeans);
    }
    return this;
  }

  public ParamsFactory addBeanWithDefaultName(Object object) {
    if (object != null) {
      addBean(uncapitalize(getPojoClass(object).getSimpleName()), object);
    }
    return this;
  }

  public Map<String, Object> getBeans() {
    return beans;
  }

  public static Map<String, Object> single(Object object) {
    return ParamsFactory.init().addBeanWithDefaultName(object).getBeans();
  }
}
