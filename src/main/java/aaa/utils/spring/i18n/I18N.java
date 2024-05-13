package aaa.utils.spring.i18n;

import static org.apache.commons.lang3.StringUtils.uncapitalize;

import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;

@AllArgsConstructor
@FieldDefaults(makeFinal = true)
public class I18N {

  Locale locale;
  MessageSource messageSource;

  // CHECKSTYLE:OFF
  public String R(String code) {
    return resolveCode(code);
  }

  // CHECKSTYLE:ON

  public String resolveCode(String code) {
    return messageSource.getMessage(code, null, locale);
  }

  public String resolveField(SingularAttribute<?, ?> attribute) {
    return resolveCode(
        ENTITY
            + uncapitalize(attribute.getDeclaringType().getJavaType().getSimpleName())
            + "."
            + uncapitalize(attribute.getName()));
  }

  public String resolveEntity(ManagedType<?> declaringType) {
    return resolveCode(ENTITY + uncapitalize(declaringType.getJavaType().getSimpleName()));
  }

  public String resolveEntity(SingularAttribute<?, Long> attribute) {
    assert "id".equals(attribute.getName());
    return resolveCode(
        ENTITY + uncapitalize(attribute.getDeclaringType().getJavaType().getSimpleName()));
  }

  public String getLocaleCode() {
    return locale.getLanguage();
  }

  public Locale toJavaLocale() {
    return locale;
  }

  public static final String ENTITY = "entity.";
}
