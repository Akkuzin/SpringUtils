package aaa.utils.spring.security;

import static aaa.utils.spring.security.PeriodicalAuthentication.PERIODICAL_AUTHENTICATION;

import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PeriodicalUtils {

  public void runTaskAsRoot(String taskName, Runnable runnable) {
    ExecuteUtils.withAuth(PERIODICAL_AUTHENTICATION)
        .doRun(
            () -> {
              try {
                runnable.run();
              } catch (Throwable e) {
                log.error("Ошибка при запуске периодического задания: " + taskName, e);
              }
            });
  }

  public <V> V callTaskAsRoot(String taskName, Callable<V> callable) {
    return ExecuteUtils.withAuth(PERIODICAL_AUTHENTICATION)
        .doCall(
            () -> {
              try {
                return callable.call();
              } catch (Throwable e) {
                log.error("Ошибка при запуске периодического задания: " + taskName, e);
                return null;
              }
            });
  }
}
