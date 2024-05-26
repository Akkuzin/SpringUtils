package aaa.utils.spring.integration.jpa;

import static aaa.nvl.Nvl.nvl;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hibernate.jpa.HibernateHints.HINT_CACHEABLE;
import static org.hibernate.jpa.HibernateHints.HINT_CACHE_MODE;
import static org.hibernate.jpa.HibernateHints.HINT_CACHE_REGION;
import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;

import aaa.utils.spring.integration.jpa.JpaUtils.EntityGraphBuilder;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import java.io.Serializable;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.experimental.WithBy;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(staticName = "build")
@Builder(toBuilder = true)
@Getter
@Setter
public class QueryParams<T> {

  @WithBy @With Specification<T> spec;
  @WithBy @With Pageable pageable;

  EntityGraphType entityGraphType;
  EntityGraph<T> entityGraph;
  String entityGraphName;
  @Default @WithBy EntityGraphBuilder<T> load = EntityGraphBuilder.builder();

  @Default @WithBy EntityGraphBuilder<T> select = EntityGraphBuilder.builder();

  @With Boolean cacheable;
  String queryCacheRegion;

  @With boolean noFlush;

  @With LockModeType lockMode;

  public static <ID extends Serializable & Comparable<ID>, T extends IAbstractPOJO<ID>>
      QueryParams<T> forId(ID id) {
    return QueryParams.forFilter(IdFilter.forId(id));
  }

  public static <ID extends Serializable & Comparable<ID>, T extends IAbstractPOJO<ID>>
      QueryParams<T> forIds(Collection<ID> ids) {
    return forFilter(IdFilter.forIds(ids));
  }

  public static <
          ID extends Serializable & Comparable<ID>,
          T extends IAbstractPOJO<ID>,
          F extends IAbstractFilter<ID, T>>
      QueryParams<T> forFilter(F filter) {
    return QueryParams.<T>build().withExtraSpec(filter);
  }

  public QueryParams<T> withUnpagedSort(Sort sort) {
    return withPageable(Pageable.unpaged(sort));
  }

  public QueryParams<T> firstBySort(Sort sort) {
    return firstNBySort(1, sort);
  }

  public QueryParams<T> firstNBySort(int count, Sort sort) {
    return withPageable(PageRequest.of(0, count, sort));
  }

  public QueryParams<T> withExtraSpec(Specification<T> extraSpec) {
    return withSpec(spec == null ? extraSpec : Specification.allOf(spec, extraSpec));
  }

  public QueryParams<T> withLoadEntityGraph(EntityGraph entityGraph) {
    return withEntityGraph(EntityGraphType.LOAD, entityGraph);
  }

  public QueryParams<T> withLoadEntityGraph(String entityGraphName) {
    return withEntityGraph(EntityGraphType.LOAD, entityGraphName);
  }

  public QueryParams<T> withEntityGraph(EntityGraphType entityGraphType, EntityGraph entityGraph) {
    return toBuilder()
        .entityGraphType(entityGraphType)
        .entityGraph(entityGraph)
        .entityGraphName(null)
        .build();
  }

  public QueryParams<T> withEntityGraph(EntityGraphType entityGraphType, String entityGraphName) {
    return toBuilder()
        .entityGraphType(entityGraphType)
        .entityGraph(null)
        .entityGraphName(entityGraphName)
        .build();
  }

  public QueryParams<T> withQueryCacheRegion(String queryCacheRegion) {
    return toBuilder().cacheable(true).queryCacheRegion(queryCacheRegion).build();
  }

  public QueryParams<T> withoutCache() {
    return withCacheable(false);
  }

  public QueryParams<T> noFlush() {
    return withNoFlush(true);
  }

  public QueryParams<T> forUpdate() {
    return withLockMode(LockModeType.PESSIMISTIC_WRITE);
  }

  public <Q extends Query> Q applyToQuery(Q query, EntityManager em, Class<T> domainClass) {
    if (entityGraph != null || isNotBlank(entityGraphName) || !load.paths.isEmpty()) {
      query.setHint(
          nvl(entityGraphType, EntityGraphType.LOAD).getKey(),
          ofNullable(entityGraph)
              .or(
                  () ->
                      ofNullable(entityGraphName)
                          .filter(StringUtils::isNotBlank)
                          .map(name -> (EntityGraph<T>) em.getEntityGraph(name)))
              .orElseGet(() -> load.build(em, domainClass)));
    }
    if (nvl(cacheable, false)) {
      query.setHint(HINT_CACHE_MODE, CacheMode.NORMAL);
      query.setHint(HINT_CACHEABLE, Boolean.TRUE);
      if (isNotBlank(queryCacheRegion)) {
        query.setHint(HINT_CACHE_REGION, queryCacheRegion);
      }
    }
    if (noFlush) {
      query.setFlushMode(FlushModeType.COMMIT);
    }
    if (getLockMode() != null) {
      query.setLockMode(getLockMode());
    }
    return query;
  }
}
