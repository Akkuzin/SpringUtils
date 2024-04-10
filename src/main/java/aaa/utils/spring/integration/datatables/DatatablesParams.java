package aaa.utils.spring.integration.datatables;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import aaa.format.SafeParse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class DatatablesParams {

  static final String S_ECHO = "sEcho";
  static final String I_COLUMNS = "iColumns";
  static final String I_DISPLAY_START = "iDisplayStart";
  static final String I_DISPLAY_LENGTH = "iDisplayLength";
  static final String M_DATA_PROP = "mDataProp_";
  static final String S_SEARCH = "sSearch";
  static final String S_COLUMN_SEARCH = "sSearch_";
  static final String B_SEARCHABLE = "bSearchable_";
  static final String B_SORTABLE = "bSortable_";
  static final String I_SORT_COL = "iSortCol_";
  static final String S_SORT_DIR = "sSortDir_";
  static final String I_SORTING_COLS = "iSortingCols";

  String search;
  int displayStart;
  int displaySize;
  List<Column> columns;
  LinkedHashMap<Column, SortDirection> sortingColumns;
  Map<Column, String> searchColumns;
  int echo;

  public static DatatablesParams getFromRequest(@NonNull HttpServletRequest request) {
    List<Column> columns = columns(request);
    return DatatablesParams.builder()
        .search(request.getParameter(S_SEARCH))
        .displayStart(parseInt(request.getParameter(I_DISPLAY_START)))
        .displaySize(parseInt(request.getParameter(I_DISPLAY_LENGTH)))
        .columns(columns)
        .sortingColumns(sortColumns(request, columns))
        .searchColumns(searchColumns(request, columns))
        .echo(parseInt(request.getParameter(S_ECHO)))
        .build();
  }

  static List<Column> columns(HttpServletRequest request) {
    return IntStream.range(0, parseInt(request.getParameter(I_COLUMNS)))
        .mapToObj(
            i ->
                Column.builder()
                    .name(request.getParameter(M_DATA_PROP + i))
                    .filterable(Boolean.parseBoolean(request.getParameter(B_SEARCHABLE + i)))
                    .sortable(Boolean.parseBoolean(request.getParameter(B_SORTABLE + i)))
                    .build())
        .collect(toList());
  }

  static LinkedHashMap<Column, SortDirection> sortColumns(
      HttpServletRequest request, List<Column> columns) {
    return IntStream.range(0, parseInt(request.getParameter(I_SORTING_COLS)))
        .boxed()
        .collect(
            toMap(
                i -> columns.get(parseInt(request.getParameter(I_SORT_COL + i))),
                i ->
                    ofNullable(request.getParameter(S_SORT_DIR + i))
                        .filter(StringUtils::isNotBlank)
                        .map(String::toUpperCase)
                        .map(SortDirection::valueOf)
                        .orElse(SortDirection.ASC),
                (e1, e2) -> e2,
                LinkedHashMap::new));
  }

  static Map<Column, String> searchColumns(HttpServletRequest request, List<Column> columns) {
    return IntStream.range(0, columns.size())
        .mapToObj(i -> Pair.of(columns.get(i), request.getParameter(S_COLUMN_SEARCH + i)))
        .filter(pair -> isNotBlank(pair.getRight()))
        .collect(toMap(Pair::getLeft, Pair::getRight));
  }

  static int parseInt(String str) {
    return SafeParse.parseInt(str, -1);
  }
}
