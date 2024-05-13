package aaa.utils.spring.security.config;

import aaa.utils.files.upload.MaxUploadSizeExceededExceptionHandler;
import java.util.Locale;
import org.springframework.jmx.support.MBeanServerFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ConfigUtils {

  public static ViewResolver makeInternalViewResolver(String path) {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setPrefix(path);
    resolver.setSuffix(".jsp");
    return resolver;
  }

  public static MaxUploadSizeExceededExceptionHandler makeUploadExceedHandler(long max) {
    return MaxUploadSizeExceededExceptionHandler.builder().maxLengthInMegabytes(max).build();
  }

  public static LocaleChangeInterceptor makeLocaleChangeInterceptor(String code) {
    LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName(code);
    return localeChangeInterceptor;
  }

  public static CookieLocaleResolver makeLocaleResolver(
      String cookieName, String defaultLocaleCode) {
    CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver(cookieName);
    cookieLocaleResolver.setDefaultLocale(new Locale(defaultLocaleCode));
    return cookieLocaleResolver;
  }

  public static MBeanServerFactoryBean makeMBeanServerFactory() {
    MBeanServerFactoryBean factory = new MBeanServerFactoryBean();
    factory.setLocateExistingServerIfPossible(true);
    return factory;
  }
}
