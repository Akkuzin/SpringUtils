package aaa.utils.spring.errors;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@Builder
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class FieldInfo<T> {
  @NonNull String context;
  @NonNull String field;
  String defaultTargetName;
}
