package aaa.utils.spring.integration.jpa;


import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationMaker {

  default Specification makeSpecification(String field, Object value) {
    return makeSpecification(JpaUtils.makePathResolver(field), value);
  }

  Specification makeSpecification(Function<Root, Path> field, Object value);
}
