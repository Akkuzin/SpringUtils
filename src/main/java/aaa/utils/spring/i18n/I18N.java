package aaa.utils.spring.i18n;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import aaa.i18n.I18NResolver;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;

@AllArgsConstructor
@FieldDefaults(makeFinal = true)
public class I18N implements I18NResolver {

  Locale locale;
  MessageSource messageSource;

  // CHECKSTYLE:OFF
  public String R(String code) {
    return resolveCode(code);
  }

  // CHECKSTYLE:ON

  public String resolveCode(String code, String defaultValue) {
    if (code == null) {
      return null;
    }
    String message = messageSource.getMessage(code, null, defaultValue, locale);
    if (message == null
        && defaultValue == null
        && startsWith(code, ENTITY)
        && countMatches(code, ".") == 2) {
      message =
          messageSource.getMessage(ENTITY + substringAfterLast(code, "."), null, null, locale);
    }
    return message;
  }

  public String resolveField(SingularAttribute<?, ?> attribute) {
    return ofNullable(
            resolveCode(
                ENTITY
                    + uncapitalize(attribute.getDeclaringType().getJavaType().getSimpleName())
                    + "."
                    + uncapitalize(attribute.getName())))
        .orElseGet(() -> resolveEntity(attribute.getJavaType()));
  }

  public String resolveEntity(ManagedType<?> declaringType) {
    return resolveEntity(declaringType.getJavaType());
  }

  public String resolveEntity(Class clazz) {
    return resolveCode(ENTITY + uncapitalize(clazz.getSimpleName()));
  }

  public String resolveEntity(SingularAttribute<?, Long> attribute) {
    assert "id".equals(attribute.getName());
    return resolveEntity(attribute.getDeclaringType().getJavaType());
  }

  public String getLocaleCode() {
    return locale.getLanguage();
  }

  public Locale toJavaLocale() {
    return locale;
  }

  public static final String ENTITY = "entity.";
}
