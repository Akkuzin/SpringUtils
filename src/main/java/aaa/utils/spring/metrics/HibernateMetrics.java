package aaa.utils.spring.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.function.TriConsumer;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;

@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class HibernateMetrics implements MeterBinder {

  Statistics statistics;

  @Override
  public void bindTo(MeterRegistry meterRegistry) {

    Gauge.builder("hibernate.statistics.queryCount", statistics::getQueryExecutionCount)
        .description("Query execution count")
        .register(meterRegistry);

    Gauge.builder("hibernate.statistics.queryCacheHitCount", statistics::getQueryCacheHitCount)
        .description("Query cache hit count")
        .register(meterRegistry);

    Gauge.builder("hibernate.statistics.queryCacheMissCount", statistics::getQueryCacheMissCount)
        .description("Query cache miss count")
        .register(meterRegistry);

    Gauge.builder(
            "hibernate.statistics.queryExecutionMaxTime", statistics::getQueryExecutionMaxTime)
        .description("Query execution max time")
        .register(meterRegistry);

    for (String query : statistics.getQueries()) {
      QueryStatistics queryStatistics = statistics.getQueryStatistics(query);
      String[] tags = {"query", query};
      TriConsumer<String, Supplier<Number>, String> reg =
          (meta, supplier, name) ->
              Gauge.builder("hibernate.statistics.query." + meta, supplier)
                  .tags(tags)
                  .description(name)
                  .register(meterRegistry);
      reg.accept("count", queryStatistics::getExecutionCount, "Query execution count");
      reg.accept("hit", queryStatistics::getCacheHitCount, "Query cache exec hit count");
      reg.accept("miss", queryStatistics::getCacheHitCount, "Query cache exec miss count");
      // reg.accept("avg", queryStatistics::getExecutionAvgTime, "Query cache exec avg time");
      reg.accept("avg", queryStatistics::getExecutionAvgTimeAsDouble, "Query cache exec avg time");
      reg.accept("rows", queryStatistics::getExecutionRowCount, "Query rows count");
      reg.accept("rows", queryStatistics::getExecutionRowCount, "Query rows count");
      reg.accept("plan.hit", queryStatistics::getPlanCacheHitCount, "Query plan cache hit count");
      reg.accept(
          "plan.miss", queryStatistics::getPlanCacheMissCount, "Query plan cache miss count");
      reg.accept(
          "plan.compileTime",
          queryStatistics::getPlanCompilationTotalMicroseconds,
          "Query plan compilation time");
    }
  }
}
