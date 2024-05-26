package aaa.utils.spring.integration.jpa;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;

import aaa.utils.spring.integration.jpa.JpaUtils.EntityGraphBuilder.PseudoSubgraph;
import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public class RepositoryWithEntityGraphImpl<T, ID extends Serializable>
    extends BaseJpaRepositoryImpl<T, ID> implements RepositoryWithEntityGraph<T, ID> {

  private EntityManager em;

  public RepositoryWithEntityGraphImpl(
      JpaEntityInformation<T, ID> entityInformation, EntityManager em) {
    super(entityInformation, em);
    this.em = em;
  }

  private Function<? super T, ? extends T> selectMapper(QueryParams<T> params) {
    return params.select.paths.isEmpty()
        ? identity()
        : value ->
            JpaUtils.selectSubgraph(
                value, PseudoSubgraph.of(params.select.build(em, getDomainClass())));
  }

  @Override
  public Optional<T> findOne(QueryParams<T> params) {
    try {
      return Optional.of(
              applyQueryParams(
                      params,
                      getQuery(
                              params.getSpec(),
                              ofNullable(params.getPageable())
                                  .map(Pageable::getSort)
                                  .orElseGet(Sort::unsorted))
                          .setMaxResults(2))
                  .getSingleResult())
          .map(selectMapper(params));
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<T> findAny(QueryParams<T> params) {
    try {
      return applyQueryParams(
              params,
              getQuery(
                      params.getSpec(),
                      ofNullable(params.getPageable())
                          .map(Pageable::getSort)
                          .orElseGet(Sort::unsorted))
                  .setMaxResults(1))
          .getResultStream()
          .findFirst()
          .map(selectMapper(params));
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  @Override
  public Page<T> findAll(@NonNull QueryParams<T> params) {
    return readPage(
            applyQueryParams(
                params,
                getQuery(
                    params.getSpec(),
                    ofNullable(params.getPageable())
                        .map(Pageable::getSort)
                        .map(
                            sort -> {
                              if (sort == null || sort.isUnsorted()) {
                                return sort;
                              }
                              Optional<Order> last = sort.stream().reduce((v1, v2) -> v2);
                              return last.map(Order::getProperty).equals(Optional.of("id"))
                                  ? sort
                                  : sort.and(
                                      Sort.by(
                                          last.map(Order::getDirection).orElse(Direction.DESC),
                                          "id"));
                            })
                        .orElseGet(Sort::unsorted))),
            getDomainClass(),
            ofNullable(params.getPageable()).orElseGet(Pageable::unpaged),
            params.getSpec())
        .map(selectMapper(params));
  }

  public long count(@NonNull QueryParams<T> params) {
    return applyQueryParams(params, getCountQuery(params.spec, getDomainClass()))
        .getResultList()
        .stream()
        .filter(Objects::nonNull)
        .reduce(0L, Long::sum);
  }

  protected <Q extends Query> Q applyQueryParams(QueryParams<T> params, Q query) {
    return params.applyToQuery(query, em, getDomainClass());
  }

  //  public long delete(QueryParams<T> params) {
  //
  //    CriteriaBuilder builder = em.getCriteriaBuilder();
  //    CriteriaDelete<T> delete = builder.createCriteriaDelete(getDomainClass());
  //
  //    if (params.getSpec() != null) {
  //      Predicate predicate =
  //          //          params.getSpec() instanceof IFilter filter
  //          //              ? IAbstractFilter.predicates(
  //          //                  filter.asPredicateStream(
  //          //                      Context.of(delete.from(getDomainClass()), delete, builder)),
  //          //                  builder)
  //          //              :
  //          params
  //              .getSpec()
  //              .toPredicate(
  //                  delete.from(getDomainClass()),
  //                  (CriteriaQuery<?>) DeleteCriteriaQuery.of(delete),
  //                  builder);
  //
  //      if (predicate != null) {
  //        delete.where(predicate);
  //      }
  //    }
  //
  //    return em.createQuery(delete).executeUpdate();
  //  }

  @Override
  public long delete(Specification<T> spec) {

    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaDelete<T> delete = builder.createCriteriaDelete(getDomainClass());

    if (spec != null) {
      Predicate predicate =
          spec.toPredicate(delete.from(getDomainClass()), DeleteCriteriaQuery.of(delete), builder);

      if (predicate != null) {
        delete.where(predicate);
      }
    }

    return em.createQuery(delete).executeUpdate();
  }

  @AllArgsConstructor(staticName = "of")
  public static class DeleteCriteriaQuery<T> implements CriteriaQuery<T> {
    CommonAbstractCriteria commonAbstractCriteria;

    @Override
    public <U> Subquery<U> subquery(Class<U> type) {
      return commonAbstractCriteria.subquery(type);
    }

    @Override
    public Predicate getRestriction() {
      return null;
    }

    @Override
    public CriteriaQuery<T> select(Selection<? extends T> selection) {
      return null;
    }

    @Override
    public CriteriaQuery<T> multiselect(Selection<?>... selections) {
      return null;
    }

    @Override
    public CriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
      return null;
    }

    @Override
    public <X> Root<X> from(Class<X> entityClass) {
      return null;
    }

    @Override
    public <X> Root<X> from(EntityType<X> entity) {
      return null;
    }

    @Override
    public CriteriaQuery<T> where(Expression<Boolean> restriction) {
      return null;
    }

    @Override
    public CriteriaQuery<T> where(Predicate... restrictions) {
      return null;
    }

    @Override
    public CriteriaQuery<T> groupBy(Expression<?>... grouping) {
      return null;
    }

    @Override
    public CriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
      return null;
    }

    @Override
    public CriteriaQuery<T> having(Expression<Boolean> restriction) {
      return null;
    }

    @Override
    public CriteriaQuery<T> having(Predicate... restrictions) {
      return null;
    }

    @Override
    public CriteriaQuery<T> orderBy(jakarta.persistence.criteria.Order... o) {
      return null;
    }

    @Override
    public CriteriaQuery<T> orderBy(List<jakarta.persistence.criteria.Order> o) {
      return null;
    }

    @Override
    public CriteriaQuery<T> distinct(boolean distinct) {
      return null;
    }

    @Override
    public Set<Root<?>> getRoots() {
      return Set.of();
    }

    @Override
    public Selection<T> getSelection() {
      return null;
    }

    @Override
    public List<Expression<?>> getGroupList() {
      return List.of();
    }

    @Override
    public Predicate getGroupRestriction() {
      return null;
    }

    @Override
    public boolean isDistinct() {
      return false;
    }

    @Override
    public Class<T> getResultType() {
      return null;
    }

    @Override
    public List<jakarta.persistence.criteria.Order> getOrderList() {
      return List.of();
    }

    @Override
    public Set<ParameterExpression<?>> getParameters() {
      return Set.of();
    }
  }
}
