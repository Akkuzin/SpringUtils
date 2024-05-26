package aaa.utils.spring.integration.jpa;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface RepositoryWithEntityGraph<T, ID extends Serializable>
    extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

  Optional<T> findOne(QueryParams<T> queryParams);

  Optional<T> findAny(QueryParams<T> queryParams);

  Page<T> findAll(QueryParams<T> params);

  default List<T> findAllUnpaged(QueryParams<T> params) {
    return findAll(params).getContent();
  }

  long count(QueryParams<T> params);

  default long delete(QueryParams<T> params) {
    return delete(params.getSpec());
  }

  default boolean exists(@NonNull QueryParams<T> params) {
    return exists(params.getSpec());
  }
}
