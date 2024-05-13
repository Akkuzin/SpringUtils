package aaa.utils.spring.integration.jpa;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;

import aaa.utils.spring.integration.jpa.JpaUtils.EntityGraphBuilder;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.experimental.WithBy;
import org.glassfish.jaxb.core.v2.model.core.ID;
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
  @Default @WithBy EntityGraphBuilder<T> load = new EntityGraphBuilder<T>();

  @Default @WithBy EntityGraphBuilder<T> select = new EntityGraphBuilder<T>();

  @With Boolean cacheable;
  String queryCacheRegion;

  @With boolean noFlush;

  @With LockModeType lockMode;

  public static <T, ID> QueryParams<T> forId(ID id) {
    return QueryParams.<T>builder()
        .build()
        .withExtraSpec(
            (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                cb.equal(root.get("id"), id));
  }

  public QueryParams<T> withUnpagedSort(Sort sort) {
    return withPageable(Pageable.unpaged(sort));
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
}
