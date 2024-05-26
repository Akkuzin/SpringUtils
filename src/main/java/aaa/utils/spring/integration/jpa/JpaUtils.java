package aaa.utils.spring.integration.jpa;

import static aaa.utils.spring.integration.jpa.AbstractPOJOUtils.getPojoClass;
import static aaa.utils.spring.integration.jpa.DaoUtils.initialize;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

import aaa.lang.reflection.IntrospectionUtils;
import aaa.utils.spring.integration.jpa.JpaUtils.EntityGraphBuilder.PseudoSubgraph;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.persistence.AttributeNode;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.With;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;

public class JpaUtils {

  public static Function<Root, Path> makePathResolver(String field) {
    return root -> {
      if (isBlank(field)) {
        return root;
      }
      From currentJoin = root;
      String[] parts = split(field, ".");
      for (int i = 0; i < parts.length - 1; i++) {
        currentJoin = currentJoin.join(parts[i], JoinType.LEFT);
      }
      return currentJoin.get(parts[parts.length - 1]);
    };
  }

  public enum LikeMatchMode {
    ANYWHERE {
      public String makeTemplate(String value) {
        return "%" + value + "%";
      }
    },
    STARTS_WITH {
      public String makeTemplate(String value) {
        return value + "%";
      }
    },
    ENDS_WITH {
      public String makeTemplate(String value) {
        return "%" + value;
      }
    },
    EQUAL {
      public String makeTemplate(String value) {
        return value;
      }
    };

    public abstract String makeTemplate(String value);
  }

  public static <X, Y> String path(Attribute<X, Y> field) {
    return field.getName();
  }

  public static <X, Y, Z> String path(Attribute<X, Y> field, Attribute<Y, Z> subfield) {
    return field.getName() + "." + subfield.getName();
  }

  public static <Y, Z> String path(String field, Attribute<Y, Z> subfield) {
    return field + "." + subfield.getName();
  }

  public static <X, Y, Z, Q> String path(
      Attribute<X, Y> field, Attribute<Y, Z> subfield, Attribute<Z, Q> subsubfield) {
    return field.getName() + "." + subfield.getName() + "." + subsubfield.getName();
  }

  public static <Y, Z, Q> String path(
      String field, Attribute<Y, Z> subfield, Attribute<Z, Q> subsubfield) {
    return field + "." + subfield.getName() + "." + subsubfield.getName();
  }

  static Cache<EntityGraphBuilder, EntityGraph> cache;

  static {
    setEntityGraphCacheSize(1000);
  }

  public static void setEntityGraphCacheSize(int size) {
    cache = CacheBuilder.newBuilder().maximumSize(size).build();
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(staticName = "builder")
  @EqualsAndHashCode
  public static class EntityGraphBuilder<T> {

    private @With(AccessLevel.PRIVATE) Class<T> clazz;
    List<String> paths = new ArrayList<>();

    public <Y> EntityGraphBuilder<T> path(Attribute<T, Y> field) {
      paths.add(JpaUtils.path(field));
      return this;
    }

    public <Y, Z> EntityGraphBuilder<T> path(Attribute<T, Y> field, Attribute<Y, Z> subfield) {
      paths.add(JpaUtils.path(field, subfield));
      return this;
    }

    public <Y, Z> EntityGraphBuilder<T> path(String field, Attribute<Y, Z> subfield) {
      paths.add(JpaUtils.path(field, subfield));
      return this;
    }

    public <Y, Z, Q> EntityGraphBuilder<T> path(
        Attribute<T, Y> field, Attribute<Y, Z> subfield, Attribute<Z, Q> subsubfield) {
      paths.add(JpaUtils.path(field, subfield, subsubfield));
      return this;
    }

    public <Y, Z, Q> EntityGraphBuilder<T> path(
        String field, Attribute<Y, Z> subfield, Attribute<Z, Q> subsubfield) {
      paths.add(JpaUtils.path(field, subfield, subsubfield));
      return this;
    }

    public List<String> asPaths() {
      return new ArrayList<>(paths);
    }

    @SneakyThrows
    public EntityGraph<T> build(EntityManager entityManager) {
      assert clazz != null;
      Collections.sort(paths);
      if (paths.isEmpty()) {
        return null;
      }
      return (EntityGraph<T>)
          cache.get(
              this.withClazz(clazz),
              () -> {
                PseudoSubgraph<T> subgraph =
                    PseudoSubgraph.of(entityManager.createEntityGraph(clazz));
                paths.forEach(path -> makeGraph(asList(StringUtils.split(path, '.')), subgraph));
                return subgraph.entityGraph;
              });
    }

    @SneakyThrows
    public EntityGraph<T> build(EntityManager entityManager, Class<T> clazz) {
      return this.withClazz(clazz).build(entityManager);
    }

    static void makeGraph(List<String> pathComponents, Subgraph<?> parent) {

      String attributeName = pathComponents.get(0);

      Optional<AttributeNode<?>> attributeNode =
          parent.getAttributeNodes().stream()
              .filter(node -> StringUtils.equals(node.getAttributeName(), attributeName))
              .findFirst();

      if (pathComponents.size() == 1) {
        if (attributeNode.isEmpty()) {
          parent.addAttributeNodes(attributeName);
        }
      } else {
        makeGraph(
            pathComponents.subList(1, pathComponents.size()),
            attributeNode
                .map(AttributeNode::getSubgraphs)
                .flatMap(map -> map.values().stream().findFirst())
                .orElseGet(() -> parent.addSubgraph(attributeName)));
      }
    }

    @AllArgsConstructor(staticName = "of")
    public static class PseudoSubgraph<T> implements Subgraph<T> {

      @Delegate EntityGraph<T> entityGraph;

      @Override
      public Class<T> getClassType() {
        return null;
      }
    }
  }

  static <T> T selectSubgraph(T pojo, Subgraph<T> subgraph) {
    subgraph
        .getAttributeNodes()
        .forEach(
            attributeNode -> {
              Object fieldValue =
                  initialize(
                      IntrospectionUtils.asGetter(
                              getPojoClass(pojo), attributeNode.getAttributeName())
                          .apply(pojo));
              if (fieldValue != null) {
                Stream.ofNullable(attributeNode.getSubgraphs())
                    .flatMap(subgraphs -> subgraphs.values().stream())
                    .forEach(
                        nestedSubgraph -> {
                          if (fieldValue instanceof Collection collection) {
                            collection.forEach(
                                subValue -> selectSubgraph(subValue, nestedSubgraph));
                          } else {
                            selectSubgraph(fieldValue, nestedSubgraph);
                          }
                        });
              }
            });
    return pojo;
  }

  static class PathExtractor<T> {
    LinkedList<String> stack = new LinkedList<>();
    List<String> paths = new ArrayList<>();

    List<String> iterate(Subgraph<?> graph) {
      graph
          .getAttributeNodes()
          .forEach(
              attributeNode -> {
                stack.push(attributeNode.getAttributeName());
                if (attributeNode.getSubgraphs() == null
                    || attributeNode.getSubgraphs().isEmpty()) {
                  paths.add(String.join(".", stack));
                } else {
                  attributeNode.getSubgraphs().values().forEach(subgraph -> iterate(subgraph));
                }
                stack.pop();
              });
      return paths;
    }
  }

  public static <T> List<String> graphAsPaths(EntityGraph<T> entityGraph) {
    return new PathExtractor<T>().iterate(PseudoSubgraph.of(entityGraph));
  }
}
