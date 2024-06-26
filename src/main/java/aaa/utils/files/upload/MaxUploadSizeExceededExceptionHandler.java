package aaa.utils.files.upload;

import aaa.utils.spring.web.MediaTypeUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaxUploadSizeExceededExceptionHandler implements HandlerExceptionResolver {

  public static final long DEFAULT_MAX_SIZE = 10L;

  @Setter @Getter @Builder.Default Long maxLengthInMegabytes = DEFAULT_MAX_SIZE;

  public void setMaxLengthInBytes(long length) {
    this.maxLengthInMegabytes = length / FileUtils.ONE_MB;
  }

  public Long getMaxSizeInBytes() {
    return FileUtils.ONE_MB * maxLengthInMegabytes;
  }

  public String getExceedMessage() {
    return "Размер прикрепляемого файла не должен превышать " + maxLengthInMegabytes + "Мб";
  }

  public ModelAndView resolveException(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      Exception exception) {
    if (exception instanceof MaxUploadSizeExceededException) {
      return new ModelAndView(
          new View() {
            @Override
            public void render(
                Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
                throws Exception {
              response.setCharacterEncoding(StandardCharsets.UTF_8.name());
              response.getOutputStream().write(getExceedMessage().getBytes(StandardCharsets.UTF_8));
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
