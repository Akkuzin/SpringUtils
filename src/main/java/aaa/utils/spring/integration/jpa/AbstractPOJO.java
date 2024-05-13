package aaa.utils.spring.integration.jpa;

import static java.util.Optional.ofNullable;

import jakarta.persistence.Transient;
import java.io.Serializable;

public abstract class AbstractPOJO<ID extends Serializable & Comparable<ID>>
    implements IAbstractPOJO<ID>, Cloneable, Serializable {

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  @Override
  public abstract ID getId();

  @Override
  @Transient
  public String getRepresentation() {
    return getClass().getSimpleName() + ofNullable(getId()).map(String::valueOf).orElse("<No ID>");
  }
}
