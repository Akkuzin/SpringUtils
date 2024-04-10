package aaa.utils.files.download;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Компонент для отправки клиенту файла для скачивания */
@Component
public class HttpDataSender {

  @Autowired ServletContext servletContext;
  @Autowired HttpServletRequest httpServletRequest;
  @Autowired HttpServletResponse httpServletResponse;

  /** Отправляет клиенту файл с указанными именем и содержимым */
  public void send(String fileName, byte[] data) {
    HttpUtils.DataSender.init(servletContext, httpServletRequest, httpServletResponse)
        .withName(fileName)
        .send(data);
  }

  public HttpUtils.DataSender withName(String fileName) {
    return HttpUtils.DataSender.init(servletContext, httpServletRequest, httpServletResponse)
        .withName(fileName);
  }
}
