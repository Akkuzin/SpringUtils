package aaa.utils.upload;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Builder;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import aaa.utils.spring.mvc.MediaTypeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaxUploadSizeExceededExceptionHandler implements HandlerExceptionResolver {

	public static final long DEFAULT_MAX_SIZE = 10L;

	@Setter
	@Getter
	Long maxLengthInMegabytes = DEFAULT_MAX_SIZE;

	@Setter
	Function<Long, String> messageResolver =
			(size) -> "Размер прикрепляемого файла не должен превышать " + size + "Мб";

	public void setMaxLengthInBytes(long length) {
		this.maxLengthInMegabytes = length / FileUtils.ONE_MB;
	}

	public Long getMaxSizeInBytes() {
		return FileUtils.ONE_MB * maxLengthInMegabytes;
	}

	public String getExceedMessage() {
		return messageResolver.apply(maxLengthInMegabytes);
	}

	public ModelAndView resolveException(	HttpServletRequest request,
											HttpServletResponse response,
											Object handler,
											Exception exception) {
		if (exception instanceof MaxUploadSizeExceededException) {
			return new ModelAndView(new View() {
				@Override
				public void render(	Map<String, ?> model,
									HttpServletRequest request,
									HttpServletResponse response) throws Exception {
					response.setCharacterEncoding(StandardCharsets.UTF_8.name());
					response.getOutputStream()
							.write(getExceedMessage().getBytes(StandardCharsets.UTF_8));
				}

				@Override
				public String getContentType() {
					return MediaTypeUtils.TEXT_HTML_VALUE;
				}
			});
		}
		return null;
	}
}
