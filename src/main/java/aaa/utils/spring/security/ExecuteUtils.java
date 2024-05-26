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
    return () -> lambda.get();
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
    return execWithAuth(authentication, () -> callable.call());
  }

  public <V, E extends Exception> void doRun(CustomCallable<Void, E> callable) throws E {
    execWithAuth(authentication, callable);
  }

  public <E extends Exception> void doRun(CustomRunnable<E> runnable) throws E {
    execWithAuth(
        authentication,
        (CustomCallable<Void, E>)
            () -> {
              runnable.run();
              return null;
            });
  }

  public static <V, E extends Exception> V execWithAuth(
      Authentication providedAuthentication, CustomCallable<V, E> callable) throws E {

    // SecurityContext общий у запросов одной сессии.
    // Нужно заменяеть контекст целиком, а не аутентификацию внутри контекста.
    // https://docs.spring.io/spring-security/site/docs/3.1.x/reference/springsecurity-single.html#tech-intro-sec-context-persistence
    // https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html#servlet-authentication-securitycontextholder
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

    // SecurityContext общий у запросов одной сессии.
    // Нужно заменяеть контекст целиком, а не аутентификацию внутри контекста.
    // https://docs.spring.io/spring-security/site/docs/3.1.x/reference/springsecurity-single.html#tech-intro-sec-context-persistence
    // https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html#servlet-authentication-securitycontextholder
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
        (CustomCallable<Void, Exception>)
            () -> {
              runnable.run();
              return null;
            });
  }
}
