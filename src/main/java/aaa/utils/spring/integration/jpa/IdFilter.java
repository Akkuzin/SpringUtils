package aaa.utils.spring.integration.jpa;

import jakarta.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true, access = AccessLevel.PRIVATE)
@Getter
@Setter
public class IdFilter<ID extends Serializable & Comparable<ID>, T extends IAbstractPOJO<ID>>
    implements IAbstractFilter<ID, T> {

  public static final String ID_NAME = "id";

  ID id;
  Collection<ID> ids;
  ID notId;
  Collection<ID> notIds;

  boolean noData;

  public static <ID extends Serializable & Comparable<ID>, T extends IAbstractPOJO<ID>>
      IdFilter<ID, T> forId(ID id) {
    return IdFilter.<ID, T>builder().id(id).noData(id == null).build();
  }

  public static <ID extends Serializable & Comparable<ID>, T extends IAbstractPOJO<ID>>
      IdFilter<ID, T> notId(ID id) {
    return IdFilter.<ID, T>builder().notId(id).build();
  }

  public static <ID extends Serializable & Comparable<ID>, T extends IAbstractPOJO<ID>>
      IdFilter<ID, T> forIds(Collection<ID> ids) {
    return IdFilter.<ID, T>builder().ids(ids).noData(ids == null || ids.isEmpty()).build();
  }

  public static <ID extends Serializable & Comparable<ID>, T extends IAbstractPOJO<ID>>
      IdFilter<ID, T> notIds(Collection<ID> ids) {
    return IdFilter.<ID, T>builder().notIds(ids).build();
  }

  @Override
  public Stream<Predicate> asPredicateStream(Context<T> ctx) {
    if (noData) {
      return Stream.of(ctx.cb.isNull(ctx.root.get(ID_NAME)));
    } else {
      return Stream.of(
          ctx.eq(ID_NAME, id),
          ctx.in(ID_NAME, ids),
          ctx.notEq(ID_NAME, notId),
          ctx.notIn(ID_NAME, notIds));
    }
  }
}
