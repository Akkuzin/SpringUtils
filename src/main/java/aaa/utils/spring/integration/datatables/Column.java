package aaa.utils.spring.integration.datatables;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class Column {

	String name;
	boolean sortable;
	boolean filterable;

}