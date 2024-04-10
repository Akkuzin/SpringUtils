package aaa.utils.spring.integration.jpa;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;

import jakarta.persistence.EntityGraph;
import lombok.*;
import lombok.experimental.WithBy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
public class QueryParams<T> {

  @WithBy @With Specification<T> spec;
  @With Pageable pageable;

  EntityGraphType entityGraphType;
  EntityGraph<T> entityGraph;
  String entityGraphName;

  @With Boolean cacheable;
  @With String queryCacheRegion;

  @With boolean noFlush;

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
}
