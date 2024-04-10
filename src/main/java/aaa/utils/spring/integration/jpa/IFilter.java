package aaa.utils.spring.integration.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;

public interface IFilter<T extends IAbstractPOJO> extends Specification<T> {

  default Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    return cb.and(
        asPredicateStream(root, query, cb).filter(Objects::nonNull).toArray(Predicate[]::new));
  }

  Stream<Predicate> asPredicateStream(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
