package aaa.utils.spring.web;

import static org.apache.commons.lang3.ArrayUtils.addAll;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

import java.util.stream.Stream;
import org.springframework.validation.DataBinder;

public class InitBinderUtils {

  public static String[] concatFields(String[] first, String[] second) {
    return isEmpty(first) ? second : isEmpty(second) ? first : addAll(first, second);
  }

  public static String[] concatFields(String[]... blocks) {
    return Stream.of(blocks).flatMap(Stream::of).toArray(String[]::new);
  }

  public static final void addAllowedFields(DataBinder binder, String... additionFields) {
    binder.setAllowedFields(concatFields(binder.getAllowedFields(), additionFields));
  }

  public static String[] nestedFields(String nestedPath, String[] fields) {
    return Stream.of(fields)
        .map(field -> nestedPath + "." + field)
        .toArray(size -> new String[size]);
  }
}
