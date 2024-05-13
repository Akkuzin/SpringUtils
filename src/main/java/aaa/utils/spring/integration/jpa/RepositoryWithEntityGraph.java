package aaa.utils.spring.integration.jpa;

import java.io.Serializable;
import java.util.Optional;

import io.hypersistence.utils.spring.repository.HibernateRepository;
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
}
