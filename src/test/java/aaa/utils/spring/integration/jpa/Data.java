package aaa.utils.spring.integration.jpa;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@FieldNameConstants
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
@Embeddable
@ToString
@EqualsAndHashCode
public class Data {
  String name;
  BigDecimal sum;
  Long count;
  LocalDateTime date;
}
