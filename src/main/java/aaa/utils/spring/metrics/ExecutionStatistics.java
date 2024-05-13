package aaa.utils.spring.metrics;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.ArrayUtils.subarray;
import static org.apache.commons.lang3.StringUtils.split;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource("Statistics:name=ExecutionStatistics")
public class ExecutionStatistics {

  Date from = new Date();

  @Getter
  @Setter
  @RequiredArgsConstructor
  @FieldDefaults(level = AccessLevel.PUBLIC)
  @ToString(exclude = "name")
  public static class MethodExecutionInfo implements Serializable {

    public static final int NANO_IN_MILLI = 1_000_000;
    final String name;
    long totalCount;
    long totalTime;
    long maxTime;
    double averageNanoseconds;

    public String getShortName() {
      String[] split = split(name, ".");
      return Stream.of(subarray(split, split.length - 2, split.length)).collect(joining("."));
    }

    public synchronized void addExecution(long time) {
      ++totalCount;
      totalTime += time;
      averageNanoseconds = ((double) totalTime) / totalCount;
      if (time > maxTime) {
        maxTime = time;
      }
    }

    public long getTotalTime() {
      return totalTime / NANO_IN_MILLI;
    }

    public long getMaxTime() {
      return maxTime / NANO_IN_MILLI;
    }
  }

  private final Map<String, ExecutionStatistics.MethodExecutionInfo> data =
      new ConcurrentHashMap<>();

  @ManagedAttribute
  public Date getFrom() {
    return from;
  }

  public Collection<MethodExecutionInfo> getDataList() {
    return data.values();
  }

  @ManagedAttribute
  public Map<String, String> getDataInfo() {
    return data.values().stream()
        .collect(toMap(MethodExecutionInfo::getName, MethodExecutionInfo::toString));
  }

  public String getDataInfoDebug() {
    return concat(
            Stream.of(
                String.format(
                    "%50s ;   %10s ;   %14s ;  %14s ;  %14s ;",
                    "Measure", "Count", "TotalTime", "MaxTime", "Average")),
            data.values().stream()
                .map(
                    info ->
                        String.format(
                            "%50s ;   %10d ;   %14d ;  %14d ;  %14s ;",
                            info.name,
                            info.totalCount,
                            info.getTotalTime(),
                            info.getMaxTime(),
                            info.getAverageNanoseconds())))
        .collect(joining("\n"));
  }

  @ManagedOperation
  public void reset() {
    data.clear();
  }

  public void countTime(String name, long time) {
    data.computeIfAbsent(name, MethodExecutionInfo::new).addExecution(time);
  }
}
