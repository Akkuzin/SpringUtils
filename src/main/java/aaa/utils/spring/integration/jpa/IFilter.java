package aaa.utils.spring.integration.jpa;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.lowerCase;

import aaa.nvl.Nvl;
import aaa.utils.spring.integration.jpa.JpaUtils.LikeMatchMode;
import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.SingularAttribute;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
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
import org.springframework.data.jpa.domain.Specification;

public interface IFilter<T extends IAbstractPOJO> extends Specification<T> {

  default Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    return cb.and(
        asPredicateStream(root, query, cb).filter(Objects::nonNull).toArray(Predicate[]::new));
  }

  default Stream<Predicate> asPredicateStream(
      Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    return asPredicateStream(new Context<T>(root, query, cb));
  }

  Stream<Predicate> asPredicateStream(Context<T> context);

  @AllArgsConstructor(staticName = "of")
  @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
  class Context<T> {
    Root<T> root;
    AbstractQuery<?> query;
    CriteriaBuilder cb;

    public Optional<String> forValue(String value) {
      return ofNullable(value).filter(StringUtils::isNotBlank);
    }

    public <S> Optional<S> forValue(S value) {
      return ofNullable(value);
    }

    public <S> Context<S> subContext(Root<S> subRoot, AbstractQuery<?> sub) {
      return Context.of(subRoot, sub, cb);
    }

    public Predicate like(SingularAttribute<T, String> attribute, String template) {
      return like(attribute, template, LikeMatchMode.ANYWHERE, true);
    }

    public Predicate like(
        SingularAttribute<T, String> attribute,
        String value,
        LikeMatchMode matchMode,
        boolean ignoreCase) {
      return forValue(value)
          .filter(StringUtils::isNotBlank)
          .map(
              v ->
                  cb.like(
                      ignoreCase ? cb.lower(root.get(attribute)) : root.get(attribute),
                      matchMode.makeTemplate(ignoreCase ? lowerCase(v) : v)))
          .orElse(null);
    }

    public <X> Predicate eq(SingularAttribute<T, X> attribute, X value) {
      return forValue(value).map(v -> cb.equal(root.get(attribute), v)).orElse(null);
    }

    public <X, Y> Predicate eq(
        SingularAttribute<T, X> attribute, SingularAttribute<X, Y> attribute2, Y value) {
      return forValue(value)
          .map(v -> cb.equal(root.get(attribute).get(attribute2), v))
          .orElse(null);
    }

    public <X, Y, Z> Predicate eq(
        SingularAttribute<T, X> attribute,
        SingularAttribute<X, Y> attribute2,
        SingularAttribute<Y, Z> attribute3,
        Z value) {
      return forValue(value)
          .map(v -> cb.equal(root.get(attribute).get(attribute2).get(attribute3), v))
          .orElse(null);
    }

    public <X> Predicate eqRoot(X value) {
      return forValue(value).map(v -> cb.equal(root, v)).orElse(null);
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

    public <X> Predicate isNotNull(SingularAttribute<T, X> attribute, boolean nullState) {
      return nullState ? cb.isNotNull(root.get(attribute)) : null;
    }

    public <X> Predicate isNotNull(SingularAttribute<T, X> attribute) {
      return isNotNull(attribute, true);
    }

    public <X> Predicate isTrueFalse(SingularAttribute<T, Boolean> attribute, Boolean state) {
      return state != null ? cb.equal(root.get(attribute), state) : null;
    }

    public <X> Predicate isTrue(SingularAttribute<T, Boolean> attribute, boolean state) {
      return isTrueFalse(attribute, state ? true : null);
    }

    public <X> Predicate isTrue(SingularAttribute<T, Boolean> attribute) {
      return isTrueFalse(attribute, true);
    }

    public <X> Predicate isFalse(SingularAttribute<T, Boolean> attribute, boolean state) {
      return isTrueFalse(attribute, state ? false : null);
    }

    public <X> Predicate isFalse(SingularAttribute<T, Boolean> attribute) {
      return isTrueFalse(attribute, false);
    }

    public <X> Predicate in(SingularAttribute<T, X> attribute, Collection<X> values) {
      return values == null ? null : cb.in(root.get(attribute)).in(values);
    }

    public Predicate and(Predicate... restrictions) {
      return cb.and(restrictions);
    }

    public Predicate or(Predicate... restrictions) {
      return cb.or(restrictions);
    }

    public <X> Predicate dateRange(SingularAttribute<T, X> attribute, DateRange dateRange) {
      return dateRange == null || !dateRange.isValueProvided()
          ? null
          : with(dateRange.makeSpecification(root -> root.get(attribute)));
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
  }
}
