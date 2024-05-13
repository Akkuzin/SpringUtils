package aaa.utils.spring.errors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.PrintStream;
import java.io.PrintWriter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.Errors;

@SuppressWarnings("serial")
@Getter
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class EntityServiceException extends BaseException {

  final String context;
  final Errors errors;

  public EntityServiceException(String context, Errors errors) {
    this.context = context;
    this.errors = errors;
  }

  private EntityServiceException(Errors errors) {
    this(null, errors);
  }

  public static EntityServiceException from(Errors errors) {
    return new EntityServiceException(errors);
  }

  @Override
  public void printStackTrace(PrintStream stream) {
    if (isNotBlank(context)) {
      stream.println(context);
    }
    if (errors != null) {
      stream.println(ErrorsMerger.asString(errors));
    }
    super.printStackTrace(stream);
  }

  public void printStackTrace(PrintWriter writer) {
    if (isNotBlank(context)) {
      writer.println(context);
    }
    if (errors != null) {
      writer.println(ErrorsMerger.asString(errors));
    }
    super.printStackTrace(writer);
  }

  @Override
  public String getMessage() {
    return (isNotBlank(super.getMessage()) ? super.getMessage() : "")
        + (isNotBlank(context) ? context + "\n" : "")
        + (errors != null ? ErrorsMerger.asString(errors) : "");
  }
}
