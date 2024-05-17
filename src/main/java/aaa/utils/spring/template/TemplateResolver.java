package aaa.utils.spring.template;

import static aaa.nvl.Nvl.nvlGet;
import static aaa.utils.spring.integration.jpa.AbstractPOJOUtils.getPojoClass;
import static java.util.Collections.emptySet;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import aaa.i18n.I18NResolver;
import aaa.nvl.Nvl;
import aaa.template.ITemplateResolver;
import aaa.utils.spring.i18n.I18N;
import com.google.common.collect.Streams;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class TemplateResolver implements ITemplateResolver {

  private static final VelocityEngine VELOCITY_ENGINE = makeEngine();

  MessageSource messageSource;

  private static VelocityEngine makeEngine() {
    VelocityEngine engine = null;
    try {
      Properties velocityProperties = new Properties();
      try (InputStream stream = TemplateResolver.class.getResourceAsStream("velocity.properties")) {
        velocityProperties.load(stream);
        engine = new VelocityEngine(velocityProperties);
      }
      // CHECKSTYLE:OFF
    } catch (Exception e) {
      // CHECKSTYLE:ON
      log.error("Ошибка инициализации шаблонизатора", e);
    }
    return engine;
  }

  @AllArgsConstructor
  public static class TemplateResolverSupport extends I18nResolverSupport {

    I18N i18n;

    @Override
    public String get(Object key) {
      return i18n.resolveCode((String) key);
    }
  }

  public I18N makeI18N(Locale locale) {
    return new I18N(locale, messageSource);
  }

  public I18N makeI18N() {
    return makeI18N(
        ofNullable(LocaleContextHolder.getLocale())
            .orElseGet(I18NResolver.DEFAULT_I18N::toJavaLocale));
  }

  public String resolveI18n(String message, Object[] beans, Locale locale) {
    return resolveI18n(message, extractParams(beans), locale);
  }

  public String resolveI18n(String message, Map<String, Object> beans, Locale locale) {
    return resolveI18n(message, beans, makeI18N(locale));
  }

  @RequiredArgsConstructor
  static final class NextedContext implements Context {

    final Context baseContext;
    HashMap<String, Object> data = new HashMap<>();

    @Override
    public Object put(String key, Object value) {
      return data.put(key, value);
    }

    @Override
    public Object get(String key) {
      return data.containsKey(key) ? data.get(key) : baseContext.get(key);
    }

    @Override
    public boolean containsKey(String key) {
      return data.containsKey(key) || baseContext.containsKey(key);
    }

    @Override
    public String[] getKeys() {
      return Streams.concat(data.keySet().stream(), Stream.of(baseContext.getKeys()))
          .distinct()
          .toArray(String[]::new);
    }

    @Override
    public Object remove(String key) {
      return data.remove(key);
    }
  }

  @Getter(lazy = true)
  private final Context velocityContext = makeBaseContext();

  protected Context makeBaseContext() {
    return new VelocityContext(
        new HashMap<>(
            Map.ofEntries(
                entry("dateFormat", DefaultFormatters.getDefaultDateFormatter()),
                entry("dateTimeFormat", DefaultFormatters.getDefaultDateTimeFormatter()),
                entry("numberFormat", DefaultFormatters.getDefaultNumberFormat()),
                entry("nvl", Nvl.class),
                entry("stringUtils", StringUtils.class))));
  }

  protected void populateWorkingContext(Context context, I18N i18n) {
    context.put("R", new TemplateResolverSupport(i18n));
    context.put("i18n", i18n);
    context.put("currentDate", new Date());
  }

  public TemplateResolverI18N withI18N(I18N i18n) {
    return new TemplateResolverI18N(i18n);
  }

  @AllArgsConstructor
  public class TemplateResolverI18N implements ITemplateResolver {

    I18N i18n;

    @Override
    public String resolveTemplate(String message, Map<String, Object> beans) {
      return resolveI18n(message, beans, i18n);
    }
  }

  @Override
  public String resolveTemplate(String message, Map<String, Object> beans) {
    return resolveI18n(message, beans, makeI18N(I18NResolver.DEFAULT_I18N.toJavaLocale()));
  }

  public String resolveI18n(String message, Map<String, Object> beans, I18N i18n) {
    if (isBlank(message)) {
      return message;
    }

    Context context = new VelocityContext(getVelocityContext());

    populateWorkingContext(context, i18n);

    if (beans != null) {
      beans.forEach(context::put);
    }

    StringWriter writer = new StringWriter();
    try {
      VELOCITY_ENGINE.evaluate(context, writer, "Message template resolvers", message);
    } catch (ParseErrorException | MethodInvocationException | ResourceNotFoundException e) {
      log.warn("Ошибка при разрешении шаблона сообщения " + substring(message, 0, 200), e);
    }
    return writer.toString();
  }

  @NoArgsConstructor(staticName = "init")
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class ParamsFactory {

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

  public static Map<String, Object> extractParams(Object[] args) {
    Object value = nvlGet(args, 0);
    return value instanceof Map ? (Map<String, Object>) value : null;
  }

  abstract static class I18nResolverSupport implements Map<String, String> {

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean containsKey(Object key) {
      return false;
    }

    @Override
    public boolean containsValue(Object value) {
      return false;
    }

    @Override
    public String put(String key, String value) {
      return null;
    }

    @Override
    public String remove(Object key) {
      return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {}

    @Override
    public void clear() {}

    @Override
    public Set<String> keySet() {
      return emptySet();
    }

    @Override
    public Collection<String> values() {
      return emptySet();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
      return emptySet();
    }
  }
}
