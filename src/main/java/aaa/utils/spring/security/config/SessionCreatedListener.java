package aaa.utils.spring.security.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationListener;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SessionCreatedListener implements ApplicationListener<HttpSessionCreatedEvent> {

  public static final int DEFAULT_MAX_INACTIVE_INTERVAL = (int) TimeUnit.HOURS.toSeconds(1);

  int maxInactiveInterval = DEFAULT_MAX_INACTIVE_INTERVAL;

  @Override
  public void onApplicationEvent(HttpSessionCreatedEvent httpSessionCreatedEvent) {
    httpSessionCreatedEvent.getSession().setMaxInactiveInterval(maxInactiveInterval);
  }
}
