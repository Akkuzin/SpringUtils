package aaa.utils.spring.integration.primevue;

import static org.assertj.core.api.Assertions.assertThat;

import aaa.utils.spring.integration.primevue.DatatableFilter.FieldConstraint;
import org.junit.jupiter.api.Test;

class DatatableFilterTest {

  @Test
  public void testFieldConstraintValueProvided() {
    assertThat(new FieldConstraint(" ", "EQUALS").isValueProvided()).isFalse();
    assertThat(new FieldConstraint("", "EQUALS").isValueProvided()).isFalse();
    assertThat(new FieldConstraint(null, "EQUALS").isValueProvided()).isFalse();
    assertThat(new FieldConstraint("1", "EQUALS").isValueProvided()).isTrue();
    assertThat(new FieldConstraint(" 1 ", "EQUALS").isValueProvided()).isTrue();
  }
}
