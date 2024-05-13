package aaa.utils.spring.security;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExecuteUtils {

  public interface CustomCallable<V, E extends Exception> extends Callable<V> {
    @Override
    V call() throws E;
  }

  public static <V> CustomCallable<V, RuntimeException> fromLambda(Supplier<V> lambda) {
    return new CustomCallable<V, RuntimeException>() {
      @Override
      public V call() {
        return lambda.get();
      }
    };
  }

  public interface CustomRunnable<E extends Exception> {
    void run() throws E;
  }

  Authentication authentication;

  public static ExecuteUtils withAuth(Authentication providedAuthentication) {
    return new ExecuteUtils(providedAuthentication);
  }

  public <V, E extends Exception> V doCall(CustomCallable<V, E> callable) throws E {
    return execWithAuth(authentication, callable);
  }

  @SneakyThrows
  public <V> V doCallSneaky(Callable<V> callable) {
    return execWithAuth(
        authentication,
        new CustomCallable<V, Exception>() {
          @Override
          public V call() throws Exception {
            return callable.call();
          }
        });
  }

  public <V, E extends Exception> void doRun(CustomCallable<Void, E> callable) throws E {
    execWithAuth(authentication, callable);
  }

  public <E extends Exception> void doRun(CustomRunnable<E> runnable) throws E {
    execWithAuth(
        authentication,
        new CustomCallable<Void, E>() {
          @Override
          public Void call() throws E {
            runnable.run();
            return null;
          }
        });
  }

  public static <V, E extends Exception> V execWithAuth(
      Authentication providedAuthentication, CustomCallable<V, E> callable) throws E {
    // FIXED На самом деле SecurityContext расшарен между запросами одной сессии. Таким образом,
    //  временно установленный здесь контекст будет подменён при параллельном запросе из другой
    //  вкладки браузера.
    //  См.
    // https://docs.spring.io/spring-security/site/docs/3.1.x/reference/springsecurity-single.html#tech-intro-sec-context-persistence

    // NB здесь заменяется контекст целиком, а не аутентификация внутри существующего контекста.
    //  Так велит делать гайд Spring Security:
    // https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html#servlet-authentication-securitycontextholder
    // It is important to create a new SecurityContext instance instead of using
    // SecurityContextHolder.getContext().setAuthentication(authentication) to avoid race conditions
    // across multiple threads.

    var oldContext = SecurityContextHolder.getContext();
    try {
      var newContext = SecurityContextHolder.createEmptyContext();
      newContext.setAuthentication(providedAuthentication);
      SecurityContextHolder.setContext(newContext);
      return callable.call();
    } finally {
      SecurityContextHolder.setContext(oldContext);
    }
  }

  public static <V> Future<V> execWithAuth(
      Authentication providedAuthentication, ExecutorService executor, Supplier<V> supplier) {
    // FIXED На самом деле SecurityContext расшарен между запросами одной сессии. Таким образом,
    //  временно установленный здесь контекст будет подменён при параллельном запросе из другой
    //  вкладки браузера.
    //  См.
    // https://docs.spring.io/spring-security/site/docs/3.1.x/reference/springsecurity-single.html#tech-intro-sec-context-persistence

    // NB здесь заменяется контекст целиком, а не аутентификация внутри существующего контекста.
    //  Так велит делать гайд Spring Security:
    // https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html#servlet-authentication-securitycontextholder
    // It is important to create a new SecurityContext instance instead of using
    // SecurityContextHolder.getContext().setAuthentication(authentication) to avoid race conditions
    // across multiple threads.

    return executor.submit(
        () -> {
          var oldContext = SecurityContextHolder.getContext();
          try {
            var newContext = SecurityContextHolder.createEmptyContext();
            newContext.setAuthentication(providedAuthentication);
            SecurityContextHolder.setContext(newContext);
            return supplier.get();
          } catch (Throwable ex) {
            return null;
          } finally {
            SecurityContextHolder.setContext(oldContext);
          }
        });
  }

  @SneakyThrows
  public static void execWithAuth(Authentication providedAuthentication, Runnable runnable) {
    execWithAuth(
        providedAuthentication,
        new CustomCallable<Void, Exception>() {
          @Override
          public Void call() throws Exception {
            runnable.run();
            return null;
          }
        });
  }
}
