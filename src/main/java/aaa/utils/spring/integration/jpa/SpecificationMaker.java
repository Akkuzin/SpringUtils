package aaa.utils.spring.integration.jpa;

import static org.apache.commons.lang3.StringUtils.split;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationMaker {

  default Specification makeSpecification(String field, Object value) {
    return makeSpecification(JpaUtils.makePathResolver(field), value);
  }

  abstract Specification makeSpecification(Function<Root, Path> field, Object value);

}
