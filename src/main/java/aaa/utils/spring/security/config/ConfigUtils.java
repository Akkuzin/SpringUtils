package aaa.utils.spring.security.config;

import java.util.Locale;

import org.springframework.jmx.support.MBeanServerFactoryBean;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import aaa.utils.upload.MaxUploadSizeExceededExceptionHandler;

public class ConfigUtils {

	public static ViewResolver makeInternalViewResolver(String path) {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix(path);
		resolver.setSuffix(".jsp");
		return resolver;
	}

	public static MultipartResolver makeMultipartResolver(long size) {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.getFileUpload().setFileSizeMax(size);
		resolver.getFileUpload().setSizeMax(size);
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

	public static CookieLocaleResolver makeLocaleResolver(	String cookieName,
															String defaultLocaleCode) {
		CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
		cookieLocaleResolver.setCookieName(cookieName);
		cookieLocaleResolver.setDefaultLocale(new Locale(defaultLocaleCode));
		return cookieLocaleResolver;
	}

	public static MBeanServerFactoryBean makeMBeanServerFactory() {
		MBeanServerFactoryBean factory = new MBeanServerFactoryBean();
		factory.setLocateExistingServerIfPossible(true);
		return factory;
	}

}
