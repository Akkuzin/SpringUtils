package aaa.utils.spring.integration.datatables;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public final class DatatablesDataSet<T> {

	List<T> rows;
	int totalDisplayRecords;
	int totalRecords;

	public DatatablesResponse<T> asResponse(int echo) {
		return DatatablesResponse.build(this, echo);
	}

}