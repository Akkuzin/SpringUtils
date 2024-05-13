package aaa.utils.spring.metrics;

import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class TimeExecutionProfiler {

  ExecutionStatistics executionStatistics;

  public Object profile(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.nanoTime();
    try {
      return joinPoint.proceed();
    } finally {
      long elapsedTime = System.nanoTime() - start;
      executionStatistics.countTime(
          substringBefore(joinPoint.getTarget().getClass().getName(), "$$")
              + "."
              + joinPoint.getSignature().getName(),
          elapsedTime);
    }
  }

  static final ThreadLocal<LinkedList<Long>> POINTS = ThreadLocal.withInitial(LinkedList::new);

  public void start() {
    POINTS.get().push(System.nanoTime());
  }

  public Optional<Long> finish(String pointName) {
    long finish = System.nanoTime();
    if (POINTS.get().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(finish - POINTS.get().pop());
  }

  public void measure(String name, Runnable runnable) {
    measure(
        name,
        (Callable<Void>)
            () -> {
              runnable.run();
              return null;
            });
  }

  @SneakyThrows
  public <V> V measure(String name, Callable<V> callable) {
    start();
    try {
      return callable.call();
    } finally {
      try {
        finish(name).ifPresent(duration -> executionStatistics.countTime(name, duration));
      } catch (Throwable t) {
        log.error("Execution statistics error", t);
      }
    }
  }
}
