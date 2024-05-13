package aaa.utils.spring.integration.jpa;

import jakarta.persistence.EntityManager;
import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class RepositoryWithEntityGraphFactoryBean<
        R extends JpaRepository<T, ID>, T, ID extends Serializable>
    extends JpaRepositoryFactoryBean<R, T, ID> {

  public RepositoryWithEntityGraphFactoryBean(Class<? extends R> repositoryInterface) {
    super(repositoryInterface);
  }

  protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
    return new CustomRepositoryFactory(entityManager);
  }

  private static class CustomRepositoryFactory extends JpaRepositoryFactory {

    private EntityManager entityManager;

    public CustomRepositoryFactory(EntityManager entityManager) {
      super(entityManager);
      this.entityManager = entityManager;
    }

    protected Object getTargetRepository(RepositoryMetadata metadata) {
      return new RepositoryWithEntityGraphImpl(
          JpaEntityInformationSupport.getEntityInformation(metadata.getDomainType(), entityManager),
          entityManager);
    }

    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
      // The RepositoryMetadata can be safely ignored, it is used by the JpaRepositoryFactory
      // to check for QueryDslJpaRepository's which is out of scope.
      return RepositoryWithEntityGraphImpl.class;
    }
  }
}
