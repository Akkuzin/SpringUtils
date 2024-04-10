package aaa.utils.spring.integration.primevue;

import static aaa.i18n.ru.TransliterateUtils.variateFix;
import static aaa.nvl.Nvl.nvl;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.split;

import aaa.utils.spring.integration.jpa.IAbstractPOJO;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public abstract class MatchMode {

  @Getter final List<String> aliases;

  public String getAlias() {
    return aliases.get(0);
  }

  public MatchMode(@NonNull List<String> aliases) {
    this.aliases =
        aliases.stream().flatMap(alias -> Stream.of(alias.replaceAll("_", ""))).distinct().toList();
  }

  static Map<String, MatchMode> MATCH_MODES;

  public static final void addMatchMode(MatchMode... matchModes) {
    Builder<String, MatchMode> builder =
        ImmutableMap.<String, MatchMode>builder().putAll(nvl(MATCH_MODES, emptyMap()));
    Stream.of(matchModes)
        .forEach(matchMode -> matchMode.aliases.forEach(alias -> builder.put(alias, matchMode)));
    MATCH_MODES = builder.build();
  }

  public static final MatchMode STARTS_WITH =
      new MatchMode(List.of("startsWith", "STARTS_WITH")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return (root, query, cb) ->
              stringCondition(
                  s -> cb.like(cb.lower(field.apply(root)), s),
                  lowerCase((String) value) + "%",
                  cb);
        }
      };

  public static final MatchMode CONTAINS =
      new MatchMode(List.of("contains", "CONTAINS")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return (root, query, cb) ->
              stringCondition(
                  s -> cb.like(cb.lower(field.apply(root)), s),
                  "%" + lowerCase((String) value) + "%",
                  cb);
        }
      };

  public static final MatchMode NOT_CONTAINS =
      new MatchMode(List.of("notContains", "NOT_CONTAINS")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return (root, query, cb) ->
              stringCondition(
                  s -> cb.notLike(cb.lower(field.apply(root)), s),
                  "%" + lowerCase((String) value) + "%",
                  cb);
        }
      };

  public static final MatchMode ENDS_WITH =
      new MatchMode(List.of("endsWith", "ENDS_WITH")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return (root, query, cb) ->
              stringCondition(
                  s -> cb.like(cb.lower(field.apply(root)), s),
                  "%" + lowerCase((String) value),
                  cb);
        }
      };

  public static final MatchMode EQUALS =
      new MatchMode(List.of("equals", "EQUALS")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return (root, query, cb) ->
              value instanceof String stringValue
                  ? stringCondition(
                      s -> cb.equal(cb.lower(field.apply(root)), s), lowerCase(stringValue), cb)
                  : cb.equal(field.apply(root), value);
        }
      };

  public static final MatchMode NOT_EQUALS =
      new MatchMode(List.of("notEquals", "NOT_EQUALS")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return (root, query, cb) ->
              value instanceof String stringValue
                  ? stringCondition(
                      s -> cb.notEqual(cb.lower(field.apply(root)), s), lowerCase(stringValue), cb)
                  : cb.notEqual(field.apply(root), value);
        }
      };

  public static final MatchMode IN =
      new MatchMode(List.of("in", "IN")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return (root, query, cb) -> {
            Class bindableJavaType = field.apply(root).getModel().getBindableJavaType();
            boolean isString = String.class.equals(bindableJavaType);
            boolean isPojo = IAbstractPOJO.class.isAssignableFrom(bindableJavaType);
            In in =
                isString
                    ? cb.in(cb.lower(field.apply(root)))
                    : isPojo ? cb.in(field.apply(root).get("id")) : cb.in(field.apply(root));
            if (isPojo) {
              if (value instanceof Map mapValue) {
                ((Map<String, Object>) mapValue)
                    .entrySet().stream()
                        .filter(
                            e -> String.valueOf(((Map) e.getValue()).get("checked")).equals("true"))
                        .map(
                            e -> {
                              try {
                                return Long.parseLong(e.getKey());
                              } catch (NumberFormatException ex) {
                                return null;
                              }
                            })
                        .filter(Objects::nonNull)
                        .forEach(id -> in.value(id));
              } else if (value instanceof Collection<?> collectionValue) {
                collectionValue.stream()
                    .forEach(
                        i -> {
                          in.value(((Map) i).get("id"));
                        });
              }
            } else {
              Stream.ofNullable(value)
                  .flatMap(v -> v instanceof Collection c ? c.stream() : Stream.of(v))
                  .forEach(
                      i -> {
                        if (isString) {
                          in.value(lowerCase((String) i));
                        } else {
                          in.value(i);
                        }
                      });
            }
            return in;
          };
        }
      };

  public static final MatchMode LESS_THAN =
      new MatchMode(List.of("lt", "LESS_THAN")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return (root, query, cb) -> cb.lessThan(field.apply(root), (Comparable) value);
        }
      };

  public static final MatchMode LESS_THAN_OR_EQUAL_TO =
      new MatchMode(List.of("lte", "LESS_THAN_OR_EQUAL_TO")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return (root, query, cb) -> cb.lessThanOrEqualTo(field.apply(root), (Comparable) value);
        }
      };

  public static final MatchMode GREATER_THAN =
      new MatchMode(List.of("gt", "GREATER_THAN")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return (root, query, cb) -> cb.greaterThan(field.apply(root), (Comparable) value);
        }
      };

  public static final MatchMode GREATER_THAN_OR_EQUAL_TO =
      new MatchMode(List.of("gte", "GREATER_THAN_OR_EQUAL_TO")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return (root, query, cb) ->
              cb.greaterThanOrEqualTo(field.apply(root), (Comparable) value);
        }
      };

  public static final MatchMode BETWEEN =
      new MatchMode(List.of("between", "BETWEEN")) {
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          if (value instanceof List listValue) {
            return Specification.allOf(
                GREATER_THAN_OR_EQUAL_TO.makeSpecification(field, listValue.get(0)),
                LESS_THAN_OR_EQUAL_TO.makeSpecification(field, listValue.get(1)));
          } else {
            return Specification.allOf();
          }
        }
      };

  public static final MatchMode DATE_IS =
      new MatchMode(List.of("dateIs", "DATE_IS")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return parseDateTime((String) value)
              .map(LocalDateTime::toLocalDate)
              .<Specification>map(
                  date ->
                      (root, query, cb) ->
                          cb.and(
                              cb.greaterThanOrEqualTo(field.apply(root), date),
                              cb.lessThan(field.apply(root), date.plusDays(1))))
              .orElse(null);
        }
      };

  public static final MatchMode DATE_IS_NOT =
      new MatchMode(List.of("dateIsNot", "DATE_IS_NOT")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return parseDateTime((String) value)
              .map(LocalDateTime::toLocalDate)
              .<Specification>map(
                  date ->
                      (root, query, cb) ->
                          cb.or(
                              cb.lessThan(field.apply(root), date),
                              cb.greaterThanOrEqualTo(field.apply(root), date.plusDays(1))))
              .orElse(null);
        }
      };

  public static final MatchMode DATE_BEFORE =
      new MatchMode(List.of("dateBefore", "DATE_BEFORE")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return parseDateTime((String) value)
              .map(LocalDateTime::toLocalDate)
              .<Specification>map(date -> (root, query, cb) -> cb.lessThan(field.apply(root), date))
              .orElse(null);
        }
      };

  public static final MatchMode DATE_AFTER =
      new MatchMode(List.of("dateAfter", "DATE_AFTER")) {
        @Override
        Specification makeSpecification(Function<Root, Path> field, Object value) {
          return parseDateTime((String) value)
              .map(LocalDateTime::toLocalDate)
              .<Specification>map(
                  date ->
                      (root, query, cb) ->
                          cb.greaterThanOrEqualTo(field.apply(root), date.plusDays(1)))
              .orElse(null);
        }
      };

  static {
    addMatchMode(
        STARTS_WITH,
        CONTAINS,
        NOT_CONTAINS,
        ENDS_WITH,
        EQUALS,
        NOT_EQUALS,
        IN,
        LESS_THAN,
        LESS_THAN_OR_EQUAL_TO,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL_TO,
        BETWEEN,
        DATE_IS,
        DATE_IS_NOT,
        DATE_BEFORE,
        DATE_AFTER);
  }

  public static Optional<LocalDateTime> parseDateTime(Object value) {
    if (value instanceof String stringValue) {
      if (isNotBlank(stringValue)) {
        return Stream.of(DateTimeFormatter.ISO_INSTANT)
            .map(
                format -> {
                  try {
                    return Instant.from(format.parse(stringValue))
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                  } catch (DateTimeParseException e) {
                    return null;
                  }
                })
            .findFirst();
      }
    } else if (value instanceof TemporalAccessor temporalAccessor) {
      return Optional.of(LocalDateTime.from(temporalAccessor));
    } else if (value instanceof Date date) {
      return Optional.of(LocalDateTime.from(date.toInstant()));
    }
    return empty();
  }

  final Specification makeSpecification(String field, Object value) {
    return makeSpecification(
        root -> {
          if (isBlank(field)) {
            return root;
          }
          From currentJoin = root;
          String[] parts = split(field, ".");
          for (int i = 0; i < parts.length - 1; i++) {
            currentJoin = currentJoin.join(parts[i], JoinType.LEFT);
          }
          return currentJoin.get(parts[parts.length - 1]);
        },
        value);
  }

  static Predicate stringCondition(
      Function<String, Predicate> condition, String value, CriteriaBuilder cb) {
    return cb.or(variateFix(value).map(condition).toArray(Predicate[]::new));
  }

  abstract Specification makeSpecification(Function<Root, Path> field, Object value);

  public static Optional<MatchMode> of(String name) {
    return ofNullable(MATCH_MODES.get(name));
  }

  public boolean is(String mode) {
    return aliases.stream().anyMatch(alias -> equalsIgnoreCase(alias, mode));
  }

  public boolean is(MatchMode... modes) {
    return Stream.ofNullable(modes).flatMap(Stream::of).anyMatch(mode -> mode == this);
  }
}
