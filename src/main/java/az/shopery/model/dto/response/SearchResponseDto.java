package az.shopery.model.dto.response;

import az.shopery.utils.enums.SearchEntity;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchResponseDto {
    SearchEntity entity;
    List<Map<String, Object>> results;
    Integer totalElements;
    Integer totalPages;
    Integer currentPage;
    Integer pageSize;
    Long executionTimeMs;
}
