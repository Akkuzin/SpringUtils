package aaa.utils.spring.integration.primevue;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.*;

import aaa.utils.spring.integration.jpa.IAbstractPOJO;
import aaa.utils.spring.integration.jpa.QueryParams;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Streams;
import java.util.*;
import java.util.stream.Stream;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  public static class FieldConstraint {
    Object value;
    String matchMode;

    public boolean isValueProvided() {
      return value != null && !(value instanceof String stringValue && isBlank(stringValue));
    }

    public Specification makeSpecificationForField(String field) {
      return MatchMode.of(matchMode).map(mode -> mode.makeSpecification(field, value)).orElse(null);
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class FieldFilter extends FieldConstraint {

    String operator;

    List<FieldConstraint> constraints = new ArrayList(asList(new FieldConstraint()));

    List<FieldConstraint> extractConstraints() {
      return ofNullable(constraints).filter(Objects::nonNull).orElseGet(() -> asList(this));
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
                                                      constraint.makeSpecificationForField(
                                                          e.getKey()))
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
    return Sort.by(
        Streams.concat(
                Stream.ofNullable(extractMetaSort())
                    .filter(not(sort -> sort.isEmpty()))
                    .flatMap(sort -> sort.stream().map(SortMeta::extractOrder)),
                Stream.of(Sort.Order.desc("id")))
            .toList());
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
