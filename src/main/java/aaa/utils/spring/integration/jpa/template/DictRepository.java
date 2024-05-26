package aaa.utils.spring.integration.jpa.template;

import static java.util.Optional.ofNullable;

import aaa.utils.spring.integration.jpa.QueryParams;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DictRepository {

  @Autowired protected EntityManager em;

  @Transactional
  public <T> List<T> listDict(Class<T> domainClass, QueryParams<T> params) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<T> cq = cb.createQuery(domainClass);
    Root<T> from = cq.from(domainClass);
    cq.select(from)
        .orderBy(
            Stream.ofNullable(params.getPageable())
                .flatMap(pageable -> pageable.getSortOr(Sort.by(Order.asc("id"))).stream())
                .map(
                    sortItem ->
                        sortItem.isAscending()
                            ? cb.asc(from.get(sortItem.getProperty()))
                            : cb.desc(from.get(sortItem.getProperty())))
                .toList());
    ofNullable(params.getSpec()).ifPresent(spec -> cq.where(spec.toPredicate(from, cq, cb)));
    return params.applyToQuery(em.createQuery(cq), em, domainClass).getResultList();
  }
}
