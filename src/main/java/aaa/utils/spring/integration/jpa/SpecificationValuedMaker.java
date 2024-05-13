package aaa.utils.spring.integration.jpa;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationValuedMaker {

  default Specification makeSpecification(String field) {
    return makeSpecification(JpaUtils.makePathResolver(field));
  }

  abstract Specification makeSpecification(Function<Root, Path> field);

  boolean isValueProvided();
}
