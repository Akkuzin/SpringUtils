package aaa.utils.spring.integration.jpa;

import static aaa.nvl.Nvl.nvl;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hibernate.jpa.HibernateHints.HINT_CACHEABLE;
import static org.hibernate.jpa.HibernateHints.HINT_CACHE_MODE;
import static org.hibernate.jpa.HibernateHints.HINT_CACHE_REGION;

import aaa.utils.spring.integration.jpa.JpaUtils.EntityGraphBuilder.PseudoSubgraph;
import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
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
      TypedQuery<T> query = getQuery(params.getSpec(), Sort.unsorted()).setMaxResults(2);
      applyQueryParams(params, query);
      return Optional.of(query.getSingleResult()).map(selectMapper(params));
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<T> findAny(QueryParams<T> params) {
    try {
      TypedQuery<T> query = getQuery(params.getSpec(), Sort.unsorted()).setMaxResults(1);
      applyQueryParams(params, query);
      return query.getResultStream().findFirst().map(selectMapper(params));
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  @Override
  public Page<T> findAll(@NonNull QueryParams<T> params) {
    TypedQuery<T> query =
        getQuery(
            params.getSpec(),
            ofNullable(params.getPageable()).map(Pageable::getSort).orElseGet(Sort::unsorted));
    applyQueryParams(params, query);
    return readPage(
            query,
            getDomainClass(),
            ofNullable(params.getPageable()).orElseGet(Pageable::unpaged),
            params.getSpec())
        .map(selectMapper(params));
  }

  protected <Q extends Query> Q applyQueryParams(QueryParams<T> params, Q query) {
    if (params.getEntityGraph() != null
        || isNotBlank(params.getEntityGraphName())
        || !params.load.paths.isEmpty()) {
      query.setHint(
          nvl(params.getEntityGraphType(), EntityGraphType.LOAD).getKey(),
          ofNullable(params.getEntityGraph())
              .or(
                  () ->
                      ofNullable(params.getEntityGraphName())
                          .filter(StringUtils::isNotBlank)
                          .map(name -> (EntityGraph<T>) em.getEntityGraph(name)))
              .orElseGet(() -> params.load.build(em, getDomainClass())));
    }
    if (nvl(params.cacheable, false)) {
      query.setHint(HINT_CACHE_MODE, CacheMode.NORMAL);
      query.setHint(HINT_CACHEABLE, Boolean.TRUE);
      if (isNotBlank(params.queryCacheRegion)) {
        query.setHint(HINT_CACHE_REGION, params.queryCacheRegion);
      }
    }
    if (params.noFlush) {
      query.setFlushMode(FlushModeType.COMMIT);
    }
    if (params.getLockMode() != null) {
      query.setLockMode(params.getLockMode());
    }
    return query;
  }
}
