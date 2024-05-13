package aaa.utils.spring.integration.jpa;

import static aaa.utils.spring.integration.jpa.DaoUtils.nvlZero;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

import aaa.utils.spring.integration.jpa.Data.Fields;
import org.junit.jupiter.api.Test;

class DaoUtilsTest {

  @Test
  public void testNvlZeroAttributes() {
    assertThat(nvlZero(Data.builder().build(), Data_.COUNT).getCount()).isZero();
    assertThat(nvlZero(Data.builder().count(42L).build(), Data_.count).getCount())
        .isEqualTo(Long.valueOf(42L));
  }

  @Test
  public void testNvlZeroAttributesMultiple() {
    assertThat(
            nvlZero(
                Data.builder().count(42L).sum(ONE).build(),
                Data_.name,
                Data_.count,
                Data_.sum,
                Data_.date))
        .isEqualTo(Data.builder().name(null).count(42L).sum(ONE).date(null).build());
  }

  @Test
  public void testNvlZeroField() {
    assertThat(nvlZero(Data.builder().build(), Fields.count, Fields.sum, Fields.name))
        .extracting(Data::getCount)
        .isEqualTo(0L);
    assertThat(nvlZero(Data.builder().build(), Fields.count, Fields.sum, Fields.name))
        .extracting(Data::getSum)
        .isEqualTo(ZERO);
    assertThat(nvlZero(Data.builder().build(), Fields.name)).extracting(Data::getCount).isNull();
    assertThat(nvlZero(Data.builder().build(), Fields.count, Fields.name))
        .extracting(Data::getSum)
        .isNull();
  }
}
