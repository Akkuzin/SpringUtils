package aaa.utils.spring.integration.jpa;

import java.io.Serializable;

public interface IAbstractPOJO<ID extends Serializable & Comparable<ID>> {

  Object clone() throws CloneNotSupportedException;

  ID getId();

  String getRepresentation();
}
