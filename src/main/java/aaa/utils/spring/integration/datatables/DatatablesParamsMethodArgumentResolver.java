package aaa.utils.spring.integration.datatables;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class DatatablesParamsMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterAnnotation(Datatables.class) != null
			&& DatatablesParams.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(	MethodParameter parameter,
									ModelAndViewContainer mavContainer,
									NativeWebRequest webRequest,
									WebDataBinderFactory binderFactory) throws Exception {
		return DatatablesParams.getFromRequest((HttpServletRequest) webRequest.getNativeRequest());
	}

}