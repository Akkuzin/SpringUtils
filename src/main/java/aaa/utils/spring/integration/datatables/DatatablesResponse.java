package aaa.utils.spring.integration.datatables;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class DatatablesResponse<T> {

  List<T> aaData;
  int iTotalRecords;
  int iTotalDisplayRecords;
  int sEcho;

  public static <T> DatatablesResponse<T> build(DatatablesDataSet<T> dataSet, int echo) {
    return DatatablesResponse.<T>builder()
        .aaData(dataSet.rows)
        .iTotalRecords(dataSet.totalRecords)
        .iTotalDisplayRecords(dataSet.getTotalDisplayRecords())
        .sEcho(echo)
        .build();
  }
}
