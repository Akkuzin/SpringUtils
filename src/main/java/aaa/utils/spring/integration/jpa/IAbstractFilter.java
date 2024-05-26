package aaa.utils.spring.integration.jpa;

import static aaa.i18n.ru.TransliterateUtils.variateFix;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.stripToEmpty;

import aaa.nvl.Nvl;
import aaa.utils.spring.integration.jpa.JpaUtils.EntityGraphBuilder;
import aaa.utils.spring.integration.jpa.JpaUtils.LikeMatchMode;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.sqm.internal.SqmCriteriaNodeBuilder;
import org.springframework.data.jpa.domain.Specification;

public interface IAbstractFilter<
        ID extends Serializable & Comparable<ID>, T extends IAbstractPOJO<ID>>
    extends Specification<T> {

  default Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    return cb.and(
        asPredicateStream(root, query, cb).filter(Objects::nonNull).toArray(Predicate[]::new));
  }

  default Stream<Predicate> asPredicateStream(
      Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    return asPredicateStream(new Context<>(root, query, cb));
  }

  Stream<Predicate> asPredicateStream(Context<T> ctx);

  @AllArgsConstructor(staticName = "of")
  @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
  class Context<T> {

    public final Root<T> root;
    public final CommonAbstractCriteria query;
    public final CriteriaBuilder cb;

    public Optional<String> forValue(String value) {
      return ofNullable(value).filter(StringUtils::isNotBlank);
    }

    public <S> Optional<S> forValue(S value) {
      return ofNullable(value);
    }

    <X> Expression<X> valueAsExpression(X value) {
      return cb instanceof SqmCriteriaNodeBuilder hcb ? hcb.value(value) : cb.literal(value);
    }

    public <S> Context<S> subContext(Root<S> subRoot, AbstractQuery<?> sub) {
      return Context.of(subRoot, sub, cb);
    }

    public Predicate global(
        Optional<? extends SpecificationValuedMaker> globalSearch, Collection<String> fields) {
      return ofNullable(globalSearch)
          .flatMap(identity())
          .filter(SpecificationValuedMaker::isValueProvided)
          .map(
              fieldFilter ->
                  or(
                      fields.stream()
                          .map(field -> with(fieldFilter.makeSpecification(field)))
                          .toArray(Predicate[]::new)))
          .orElse(null);
    }

    public Predicate global(
        Optional<? extends SpecificationValuedMaker> globalSearch, String... fields) {
      return global(globalSearch, asList(fields));
    }

    public Predicate global(
        Optional<? extends SpecificationValuedMaker> globalSearch,
        EntityGraphBuilder<T> entityGraphBuilder) {
      return global(globalSearch, entityGraphBuilder.asPaths());
    }

    public Predicate global(
        Optional<? extends SpecificationValuedMaker> globalSearch, EntityGraph<T> entityGraph) {
      return global(globalSearch, JpaUtils.graphAsPaths(entityGraph));
    }

    <X> X stripValue(X value) {
      return value instanceof Collection collection
          ? (X) collection.stream().map(this::stripValue).toList()
          : value instanceof String string ? (X) stripToEmpty(string) : value;
    }

    public Predicate like(SingularAttribute<T, String> attribute, String template) {
      return like(attribute, template, LikeMatchMode.ANYWHERE, true);
    }

    public Predicate likeByWords(SingularAttribute<T, String> attribute, String template) {
      return forValue(template)
          .filter(StringUtils::isNotBlank)
          .map(this::stripValue)
          .map(
              value ->
                  and(
                      Stream.of(split(value))
                          .filter(StringUtils::isNotBlank)
                          .map(
                              word ->
                                  or(
                                      variateFix(word)
                                          .map(v -> like(attribute, v))
                                          .toArray(Predicate[]::new)))
                          .toArray(Predicate[]::new)))
          .orElse(null);
    }

    public Predicate like(
        SingularAttribute<T, String> attribute,
        String value,
        LikeMatchMode matchMode,
        boolean ignoreCase) {
      return forValue(value)
          .filter(StringUtils::isNotBlank)
          .map(this::stripValue)
          .map(
              v ->
                  cb.like(
                      ignoreCase(root.get(attribute), ignoreCase),
                      ignoreCase(valueAsExpression(matchMode.makeTemplate(v)), ignoreCase)))
          .orElse(null);
    }

    public <X> Predicate compare(
        Expression<X> e1,
        Expression<X> e2,
        BiFunction<Expression<X>, Expression<X>, Predicate> operator,
        Function<Expression<X>, Expression<X>> preprocessor) {
      return preprocessor == null
          ? operator.apply(e1, e2)
          : operator.apply(preprocessor.apply(e1), preprocessor.apply(e2));
    }

    public Expression<String> ignoreCase(Expression<String> expression, boolean ignoreCase) {
      return ignoreCase ? cb.lower(expression) : expression;
    }

    public <X> Predicate compare(
        Expression<X> expression,
        X value,
        BiFunction<Expression<X>, Expression<X>, Predicate> operator,
        Function<Expression<X>, Expression<X>> preprocessor) {
      return forValue(value)
          .map(this::stripValue)
          .map(v -> compare(expression, valueAsExpression(v), operator, preprocessor))
          .orElse(null);
    }

    public <X> Predicate eq(
        Expression<X> expression, X value, Function<Expression<X>, Expression<X>> preprocessor) {
      return compare(expression, value, cb::equal, preprocessor);
    }

    public <X> Predicate eq(SingularAttribute<T, X> attribute, X value) {
      return eq(root.get(attribute), value, null);
    }

    public Predicate eqIgnoreCase(SingularAttribute<T, String> attribute, String value) {
      return eq(root.get(attribute), value, cb::lower);
    }

    public <X> Predicate eq(String attribute, X value) {
      return eq(root.get(attribute), value, null);
    }

    public Predicate eq(String attribute, String value) {
      return eq(root.get(attribute), value, cb::lower);
    }

    public <X> Predicate notEq(
        Expression<X> expression, X value, Function<Expression<X>, Expression<X>> preprocessor) {
      return compare(expression, value, (e1, e2) -> cb.equal(e1, e2).not(), preprocessor);
    }

    public <X> Predicate notEq(SingularAttribute<T, X> attribute, X value) {
      return notEq(root.get(attribute), value, null);
    }

    public Predicate notEqIgnoreCase(SingularAttribute<T, String> attribute, String value) {
      return notEq(root.get(attribute), value, cb::lower);
    }

    public <X> Predicate notEq(String attribute, X value) {
      return notEq(root.get(attribute), value, null);
    }

    public Predicate notEqIgnoreCase(String attribute, String value) {
      return notEq(root.get(attribute), value, cb::lower);
    }

    public <X, Y> Predicate eq(
        SingularAttribute<T, X> attribute, SingularAttribute<X, Y> attribute2, Y value) {
      return eq(root.get(attribute).get(attribute2), value, null);
    }

    public <X, Y, Z> Predicate eq(
        SingularAttribute<T, X> attribute,
        SingularAttribute<X, Y> attribute2,
        SingularAttribute<Y, Z> attribute3,
        Z value) {
      return eq(root.get(attribute).get(attribute2).get(attribute3), value, null);
    }

    public Predicate eqRoot(T value) {
      return eq(root, value, null);
    }

    /** true - isNull; false - isNotNull */
    public <X> Predicate nullCondition(SingularAttribute<T, X> attribute, Boolean nullState) {
      return nullState == null
          ? null
          : nullState ? cb.isNull(root.get(attribute)) : cb.isNotNull(root.get(attribute));
    }

    public <X> Predicate isNull(SingularAttribute<T, X> attribute, boolean nullState) {
      return nullState ? cb.isNull(root.get(attribute)) : null;
    }

    public <X> Predicate isNull(SingularAttribute<T, X> attribute) {
      return isNull(attribute, true);
    }

    public <X, Y> Predicate isNull(
        SingularAttribute<T, X> attribute,
        SingularAttribute<X, Y> subattribute,
        boolean nullState) {
      return nullState ? cb.isNull(root.get(attribute).get(subattribute)) : null;
    }

    public <X, Y> Predicate isNull(
        SingularAttribute<T, X> attribute, SingularAttribute<X, Y> subattribute) {
      return isNull(attribute, subattribute, true);
    }

    public <X> Predicate isNotNull(SingularAttribute<T, X> attribute, boolean nullState) {
      return nullState ? cb.isNotNull(root.get(attribute)) : null;
    }

    public <X> Predicate isNotNull(SingularAttribute<T, X> attribute) {
      return isNotNull(attribute, true);
    }

    public <X, Y> Predicate isNotNull(
        SingularAttribute<T, X> attribute,
        SingularAttribute<X, Y> subattribute,
        boolean nullState) {
      return nullState ? cb.isNotNull(root.get(attribute).get(subattribute)) : null;
    }

    public <X, Y> Predicate isNotNull(
        SingularAttribute<T, X> attribute, SingularAttribute<X, Y> subattribute) {
      return isNotNull(attribute, subattribute, true);
    }

    public <X> Predicate isTrueFalse(
        SingularAttribute<T, X> attribute,
        SingularAttribute<X, Boolean> subattribute,
        Boolean state) {
      return state != null ? cb.equal(root.get(attribute).get(subattribute), state) : null;
    }

    public <X> Predicate isTrueFalse(SingularAttribute<T, X> attribute, Boolean state) {
      return state != null ? cb.equal(root.get(attribute), state) : null;
    }

    public Predicate isTrueFalse(String attribute, Boolean state) {
      return state != null ? cb.equal(root.get(attribute), state) : null;
    }

    public Predicate isTrue(SingularAttribute<T, Boolean> attribute, boolean state) {
      return isTrueFalse(attribute, state ? true : null);
    }

    public Predicate isTrue(SingularAttribute<T, Boolean> attribute) {
      return isTrueFalse(attribute, true);
    }

    public <X> Predicate isTrue(
        SingularAttribute<T, X> attribute,
        SingularAttribute<X, Boolean> subattribute,
        boolean state) {
      return isTrueFalse(attribute, subattribute, state ? true : null);
    }

    public <X> Predicate isTrue(
        SingularAttribute<T, X> attribute, SingularAttribute<X, Boolean> subattribute) {
      return isTrueFalse(attribute, subattribute, true);
    }

    public Predicate isFalse(SingularAttribute<T, Boolean> attribute, boolean state) {
      return isTrueFalse(attribute, state ? false : null);
    }

    public Predicate isFalse(SingularAttribute<T, Boolean> attribute) {
      return isTrueFalse(attribute, false);
    }

    public <X> Predicate isFalse(
        SingularAttribute<T, X> attribute,
        SingularAttribute<X, Boolean> subattribute,
        boolean state) {
      return isTrueFalse(attribute, subattribute, state ? false : null);
    }

    public <X> Predicate isFalse(
        SingularAttribute<T, X> attribute, SingularAttribute<X, Boolean> subattribute) {
      return isTrueFalse(attribute, subattribute, false);
    }

    <X> In<X> makeIn(In<X> in, Collection<X> values) {
      if (values.isEmpty()) {
        in.value((X) null);
      } else {
        stripValue(values).forEach(v -> in.value(v));
      }
      return in;
    }

    public <X> Predicate in(SingularAttribute<T, X> attribute, Collection<X> values) {
      return values == null ? null : makeIn(cb.in(root.get(attribute)), values);
    }

    public <X> Predicate in(String attribute, Collection<X> values) {
      return values == null ? null : makeIn(cb.in(root.get(attribute)), values);
    }

    public <X> Predicate notIn(SingularAttribute<T, X> attribute, Collection<X> values) {
      return values == null
          ? null
          : values.isEmpty() ? null : makeIn(cb.in(root.get(attribute)), values).not();
    }

    public <X> Predicate notIn(String attribute, Collection<X> values) {
      return values == null
          ? null
          : values.isEmpty() ? null : makeIn(cb.in(root.get(attribute)), values).not();
    }

    public Predicate and(Predicate... restrictions) {
      Predicate[] predicates =
          Stream.of(restrictions).filter(Objects::nonNull).toArray(Predicate[]::new);
      return predicates.length == 0 ? null : cb.and(predicates);
    }

    public Predicate or(Predicate... restrictions) {
      Predicate[] predicates =
          Stream.of(restrictions).filter(Objects::nonNull).toArray(Predicate[]::new);
      return predicates.length == 0 ? null : cb.or(predicates);
    }

    public <X> Predicate dateRange(SingularAttribute<T, X> attribute, DateRange dateRange) {
      return dateRange == null || !dateRange.isValueProvided()
          ? null
          : with(dateRange.makeSpecification(root -> root.get(attribute)));
    }

    public <X, Y> Predicate dateRange(
        SingularAttribute<T, X> attribute,
        SingularAttribute<X, Y> attribute2,
        DateRange dateRange) {
      return dateRange == null || !dateRange.isValueProvided()
          ? null
          : with(dateRange.makeSpecification(root -> root.get(attribute).get(attribute2)));
    }

    public <X, Y> Predicate eq(
        SingularAttribute<T, X> attribute,
        SingularAttribute<X, Y> attribute2,
        DateRange dateRange) {
      return with(dateRange.makeSpecification(root -> root.get(attribute).get(attribute2)));
    }

    public <X> Predicate existsLink(
        SingularAttribute<X, T> joinColumn, Function<Context<X>, Predicate> subPredicateBuilder) {
      Subquery<Long> sub = query.subquery(Long.class);
      Root subRoot = sub.from(joinColumn.getDeclaringType().getJavaType());
      sub.select(cb.literal(1L));
      Predicate subPredicate = subPredicateBuilder.apply(subContext(subRoot, sub));
      if (subPredicate == null) {
        return null;
      }
      sub.where(cb.and(cb.equal(subRoot.get(joinColumn), this.root), subPredicate));
      return cb.exists(sub);
    }

    public Predicate with(Specification<T> specification) {
      return specification.toPredicate(root, (CriteriaQuery) query, cb);
    }
  }

  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @FieldDefaults(level = AccessLevel.PUBLIC)
  class DateRange implements SpecificationValuedMaker {

    static final LocalDateTime DISTANT_PAST = LocalDate.of(1997, Month.JANUARY, 1).atStartOfDay();
    static final LocalDateTime DISTANT_FUTURE = LocalDate.of(2099, Month.JANUARY, 1).atStartOfDay();

    LocalDateTime start;
    LocalDateTime finish;
    boolean strictStart;
    boolean strictFinish;

    @Override
    public Specification makeSpecification(Function<Root, Path> field) {
      return (root, query, cb) ->
          cb.and(
              Stream.of(
                      start == null
                          ? null
                          : strictStart
                              ? cb.greaterThan(field.apply(root), start)
                              : cb.greaterThanOrEqualTo(field.apply(root), start),
                      finish == null
                          ? null
                          : strictFinish
                              ? cb.lessThan(field.apply(root), finish)
                              : cb.lessThanOrEqualTo(field.apply(root), finish))
                  .filter(Objects::nonNull)
                  .toArray(Predicate[]::new));
    }

    @Override
    public boolean isValueProvided() {
      return start != null || finish != null;
    }

    public DateRange intersect(DateRange range2) {
      DateRangeBuilder builder =
          builder()
              .start((LocalDateTime) Nvl.max(start, range2.start))
              .finish((LocalDateTime) Nvl.min(finish, range2.finish));
      return builder
          .strictStart(
              (Objects.equals(builder.start, start) && strictStart)
                  || (Objects.equals(builder.start, range2.start) && range2.strictStart))
          .strictFinish(
              (Objects.equals(builder.finish, finish) && strictFinish)
                  || (Objects.equals(builder.finish, range2.finish) && range2.strictFinish))
          .build();
    }

    public DateRange union(DateRange range2) {
      DateRangeBuilder builder =
          builder()
              .start((LocalDateTime) Nvl.min(start, range2.start))
              .finish((LocalDateTime) Nvl.max(finish, range2.finish));
      return builder
          .strictStart(
              (Objects.equals(builder.start, start) && strictStart)
                  && (Objects.equals(builder.start, range2.start) && range2.strictStart))
          .strictFinish(
              (Objects.equals(builder.finish, finish) && strictFinish)
                  && (Objects.equals(builder.finish, range2.finish) && range2.strictFinish))
          .build();
    }

    public static DateRange forDays(Temporal temporal, int days) {
      return DateRange.builder()
          .start(LocalDate.from(temporal).plusDays(Math.min(days, 0)).atStartOfDay())
          .finish(LocalDate.from(temporal).plusDays(Math.max(days, 0)).atStartOfDay())
          .strictFinish(true)
          .build();
    }

    public static DateRange forDay(Temporal temporal) {
      return forDays(temporal, 1);
    }

    public static DateRange today() {
      return forDay(LocalDate.now());
    }

    public static DateRange tomorrow() {
      return forDay(LocalDate.now().plusDays(1));
    }

    public static DateRange currentMonth() {
      LocalDate now = LocalDate.now();
      return DateRange.builder()
          .start(now.with(firstDayOfMonth()).atStartOfDay())
          .finish(now.plusMonths(1).with(firstDayOfMonth()).atStartOfDay())
          .strictFinish(true)
          .build();
    }

    public static DateRange currentYear() {
      LocalDate now = LocalDate.now();
      return DateRange.builder()
          .start(now.with(firstDayOfYear()).atStartOfDay())
          .finish(now.plusYears(1).with(firstDayOfYear()).atStartOfDay())
          .strictFinish(true)
          .build();
    }
  }
}
