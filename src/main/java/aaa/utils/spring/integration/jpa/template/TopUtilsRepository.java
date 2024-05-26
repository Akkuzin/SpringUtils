package aaa.utils.spring.integration.jpa.template;

import static com.google.common.collect.Streams.concat;
import static java.util.Optional.ofNullable;

import aaa.utils.spring.integration.jpa.QueryParams;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TopUtilsRepository {

  @Autowired EntityManager em;

  @Transactional
  public <T, V> List<V> topValues(SingularAttribute<T, V> attribute, QueryParams<T> params) {
    CriteriaBuilder cb = em.getCriteriaBuilder();

    CriteriaQuery<V> cq = cb.createQuery(attribute.getBindableJavaType());
    Class<T> domainClass = attribute.getDeclaringType().getJavaType();
    Root<T> from = cq.from(domainClass);
    Path<V> value = from.get(attribute.getName());
    cq.select(value)
        .groupBy(value)
        .orderBy(
            concat(
                    Stream.of(cb.desc(cb.count(from))),
                    Stream.ofNullable(params.getPageable())
                        .flatMap(
                            pageable ->
                                pageable
                                    .getSortOr(Sort.by(Order.asc(attribute.getName())))
                                    .stream())
                        .map(
                            sortItem ->
                                sortItem.isAscending()
                                    ? cb.asc(from.get(sortItem.getProperty()))
                                    : cb.desc(from.get(sortItem.getProperty()))))
                .toList());
    ofNullable(params.getSpec()).ifPresent(spec -> cq.where(spec.toPredicate(from, cq, cb)));
    return params
        .applyToQuery(em.createQuery(cq), em, domainClass)
        .setMaxResults(ofNullable(params.getPageable()).map(Pageable::getPageSize).orElse(100))
        .getResultList();
  }
}
