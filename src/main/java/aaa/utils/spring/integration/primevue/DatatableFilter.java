package aaa.utils.spring.integration.primevue;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

import aaa.utils.spring.integration.jpa.IAbstractPOJO;
import aaa.utils.spring.integration.jpa.QueryParams;
import aaa.utils.spring.integration.jpa.SpecificationValuedMaker;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatatableFilter<T extends IAbstractPOJO> {

  public static final String GLOBAL = "global";
  Integer first;
  Integer rows;
  String sortField;
  Integer sortOrder;
  List<SortMeta> multiSortMeta;
  Map<String, FieldFilter> filters;

  List<SortMeta> asSortMeta() {
    return isBlank(sortField)
        ? asList()
        : asList(new SortMeta(sortField, ofNullable(sortOrder).orElse(0)));
  }

  public List<SortMeta> extractMetaSort() {
    return ofNullable(multiSortMeta).filter(not(Collection::isEmpty)).orElseGet(this::asSortMeta);
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class FieldConstraint implements SpecificationValuedMaker {
    Object value;
    String matchMode;

    @Override
    public Specification makeSpecification(Function<Root, Path> field) {
      return MatchMode.of(matchMode).map(mode -> mode.makeSpecification(field, value)).orElse(null);
    }

    public boolean isValueProvided() {
      return value != null && !(value instanceof String stringValue && isBlank(stringValue));
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class FieldFilter extends DatatableFilter.FieldConstraint {

    String operator;
    List<FieldConstraint> constraints;

    List<FieldConstraint> extractConstraints() {
      return ofNullable(constraints).filter(not(Collection::isEmpty)).orElseGet(() -> asList(this));
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class SortMeta {
    String field;
    Integer order;

    Sort.Direction extractDirection(Integer order) {
      return order == null || order > 0 ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    public Sort.Order extractOrder() {
      return new Sort.Order(extractDirection(order), field);
    }
  }

  public Optional<FieldFilter> getGlobal() {
    return ofNullable(filters).orElse(emptyMap()).entrySet().stream()
        .filter(e -> equalsIgnoreCase(e.getKey(), GLOBAL))
        .findFirst()
        .map(Map.Entry::getValue)
        .filter(FieldConstraint::isValueProvided);
  }

  public Specification<T> extractSpecification() {
    return Specification.allOf(
        (List)
            ofNullable(filters).orElse(emptyMap()).entrySet().stream()
                .filter(e -> !equalsIgnoreCase(e.getKey(), GLOBAL))
                .flatMap(
                    e ->
                        Stream.ofNullable(e.getValue())
                            .map(
                                fieldFilter -> {
                                  List specs =
                                      (fieldFilter.getOperator() == null
                                              ? asList(fieldFilter)
                                              : ofNullable(fieldFilter.getConstraints())
                                                  .orElse(emptyList()))
                                          .stream()
                                              .filter(FieldConstraint::isValueProvided)
                                              .map(
                                                  constraint ->
                                                      constraint.makeSpecification(e.getKey()))
                                              .filter(Objects::nonNull)
                                              .toList();
                                  return equalsIgnoreCase(
                                          "and",
                                          ofNullable(fieldFilter.getOperator()).orElse("and"))
                                      ? Specification.allOf(specs)
                                      : Specification.anyOf(specs);
                                }))
                .toList());
  }

  Sort extractSort() {
    List<Order> orderList =
        Stream.ofNullable(extractMetaSort())
            .filter(not(List::isEmpty))
            .flatMap(sort -> sort.stream().map(SortMeta::extractOrder))
            .toList();
    Optional<Order> last = orderList.stream().reduce((v1, v2) -> v2);
    return last.map(Order::getProperty).equals(Optional.of("id"))
        ? Sort.by(orderList)
        : Sort.by(orderList)
            .and(Sort.by(last.map(Order::getDirection).orElse(Direction.DESC), "id"));
  }

  public Pageable extractPageable() {
    Integer pageSize = ofNullable(rows).orElse(DEFAULT_PAGE_SIZE);
    return PageRequest.of(ofNullable(first).orElse(0) / pageSize, pageSize, extractSort());
  }

  public QueryParams<T> extractQueryParams() {
    return QueryParams.<T>builder()
        .spec(extractSpecification())
        .pageable(extractPageable())
        .build();
  }

  public static final int DEFAULT_PAGE_SIZE = 20;
}
