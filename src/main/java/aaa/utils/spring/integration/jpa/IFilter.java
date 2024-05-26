package aaa.utils.spring.integration.jpa;

import org.springframework.data.jpa.domain.Specification;

public interface IFilter<T extends IAbstractPOJO<Long>>
    extends IAbstractFilter<Long, T>, Specification<T> {}
