package aaa.utils.spring.integration.jpa;

import static aaa.nvl.Nvl.nvl;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_CACHE_MODE;
import static org.hibernate.jpa.QueryHints.HINT_CACHE_REGION;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.util.Optional;
import lombok.NonNull;
import org.hibernate.CacheMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public class RepositoryWithEntityGraphImpl<T, ID extends Serializable>
    extends SimpleJpaRepository<T, ID> implements RepositoryWithEntityGraph<T, ID> {

  private EntityManager em;

  public RepositoryWithEntityGraphImpl(
      JpaEntityInformation<T, ID> entityInformation, EntityManager em) {
    super(entityInformation, em);
    this.em = em;
  }

  public RepositoryWithEntityGraphImpl(Class<T> domainClass, EntityManager em) {
    super(domainClass, em);
    this.em = em;
  }

  @Override
  public Optional<T> findOne(QueryParams<T> queryParams) {
    try {
      TypedQuery<T> query = getQuery(queryParams.getSpec(), Sort.unsorted()).setMaxResults(2);
      applyQueryParams(queryParams, query);
      return Optional.of(query.getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<T> findAny(QueryParams<T> queryParams) {
    try {
      TypedQuery<T> query = getQuery(queryParams.getSpec(), Sort.unsorted()).setMaxResults(1);
      applyQueryParams(queryParams, query);
      return query.getResultStream().findFirst();
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
        params.getSpec());
  }

  protected <Q extends Query> Q applyQueryParams(QueryParams<T> params, Q query) {
    if (params.getEntityGraph() != null || isNotBlank(params.getEntityGraphName())) {
      query.setHint(
          params.getEntityGraphType().getKey(),
          ofNullable(params.getEntityGraph())
              .orElseGet(() -> (EntityGraph<T>) em.getEntityGraph(params.getEntityGraphName())));
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
    return query;
  }
}
