package aaa.utils.spring.pojo;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import aaa.currency.CurrencyUtils;
import aaa.utils.spring.pojo.PojoUtils.SummatorBuilder;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PojoUtilsTest {

  @FieldDefaults(level = AccessLevel.PUBLIC)
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @Builder
  @EqualsAndHashCode
  @ToString
  public static class Data {

    BigDecimal value1;
    BigDecimal value2;
    BigDecimal valueExtra;
    Long count1;
    Long count2;
    String name;
    String desc;
    boolean mode;
    Currency currency;

    public static final BinaryOperator<Data> SUMMATOR_MANUAL =
        (d1, d2) ->
            Data.builder()
                .value1(
                    Stream.of(d1, d2)
                        .map(Data::getValue1)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal::add)
                        .orElse(ZERO))
                .value2(
                    Stream.of(d1, d2)
                        .map(Data::getValue2)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal::add)
                        .orElse(ZERO))
                .count1(
                    Stream.of(d1, d2)
                        .map(Data::getCount1)
                        .filter(Objects::nonNull)
                        .reduce(Long::sum)
                        .orElse(0L))
                .count2(
                    Stream.of(d1, d2)
                        .map(Data::getCount2)
                        .filter(Objects::nonNull)
                        .reduce(Long::sum)
                        .orElse(0L))
                .name(
                    Stream.of(d1, d2)
                        .map(Data::getName)
                        .filter(StringUtils::isNotBlank)
                        .findFirst()
                        .orElse(null))
                .mode(Stream.of(d1, d2).anyMatch(Data::isMode))
                .currency(
                    Stream.of(d1, d2)
                        .map(Data::getCurrency)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null))
                .build();

    public static final BinaryOperator<Data> SUMMATOR =
        SummatorBuilder.forClass(Data.class)
            .add(Data::getValue1)
            .add(Data::getValue2)
            .add(Data::getCount1)
            .add(Data::getCount2)
            .add(Data::getName)
            .add(Data::isMode)
            .add(Data::getCurrency)
            .build();
  }

  @Test
  public void matchRandomTest() {
    for (int seed = 10; seed > 0; --seed) {
      Random random = new Random(seed);
      List<Data> data =
          IntStream.of(0, 10_000)
              .mapToObj(
                  i ->
                      Data.builder()
                          .value1(
                              CurrencyUtils.round(
                                  BigDecimal.valueOf(random.nextDouble())
                                      .multiply(BigDecimal.valueOf(1_000_000))))
                          .value2(
                              CurrencyUtils.round(
                                  BigDecimal.valueOf(random.nextDouble())
                                      .multiply(BigDecimal.valueOf(100_000))))
                          .valueExtra(
                              CurrencyUtils.round(
                                  BigDecimal.valueOf(random.nextDouble())
                                      .multiply(BigDecimal.valueOf(100_000))))
                          .count1(random.nextLong() % 1_000)
                          .count2(random.nextLong() % 1_000_000)
                          .name(UUID.randomUUID().toString())
                          .desc(String.valueOf(random.nextLong()))
                          .mode(random.nextBoolean())
                          .build())
              .toList();
      assertEquals(
          data.stream().reduce(Data.SUMMATOR_MANUAL).get(),
          data.stream().reduce(Data.SUMMATOR).get());
    }
  }

  @SneakyThrows
  static void timed(String name, int count, Callable<Long> callable) {
    long sum = 0L;
    long start = System.nanoTime();

    for (int i = count; i > 0; --i) {
      sum += callable.call();
    }

    long finish = System.nanoTime();
    System.out.println("--------------------------" + sum);
    System.out.println(name + "\t\tms\t\t " + (finish - start) / 1_000_000);
    System.out.println(name + "\t\tnano\t\t " + (finish - start) / count);
  }

  static <T> void testTimed(
      String name, int count, Collection<T> data, BinaryOperator<T> summator) {
    timed(
        name + " " + count,
        count,
        () -> data.stream().reduce(summator).map(f -> (long) f.hashCode()).get());
  }

  @Test
  @Disabled
  public void testPerformance() {
    Random random = new Random(12345);
    List<Data> flights =
        IntStream.of(0, 10_000)
            .mapToObj(
                i ->
                    Data.builder()
                        .value1(
                            CurrencyUtils.round(
                                BigDecimal.valueOf(random.nextDouble())
                                    .multiply(BigDecimal.valueOf(1000_000))))
                        .value2(
                            CurrencyUtils.round(
                                BigDecimal.valueOf(random.nextDouble())
                                    .multiply(BigDecimal.valueOf(100_000))))
                        .valueExtra(
                            CurrencyUtils.round(
                                BigDecimal.valueOf(random.nextDouble())
                                    .multiply(BigDecimal.valueOf(100_000))))
                        .count1(random.nextLong() % 1_000)
                        .count2(random.nextLong() % 1_000_000)
                        .name(UUID.randomUUID().toString())
                        .desc(String.valueOf(random.nextLong()))
                        .mode(random.nextBoolean())
                        .build())
            .collect(toList());
    BinaryOperator<Data> plainSummator = Data.SUMMATOR_MANUAL;
    testTimed("plain", 1_000_000, flights, plainSummator);
    testTimed("plain", 5_000_000, flights, plainSummator);

    testTimed("getter", 1_000_000, flights, Data.SUMMATOR);
    testTimed("getter", 5_000_000, flights, Data.SUMMATOR);

    testTimed("plain", 5_000_000, flights, Data.SUMMATOR_MANUAL);
    testTimed("getter", 5_000_000, flights, Data.SUMMATOR);
  }
}
