package az.shopery.model.dto.response;

import az.shopery.utils.enums.SearchEntity;
import az.shopery.utils.enums.SearchOperator;
import java.util.List;
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
public class SearchMetadataResponseDto {
    List<EntityMetadata> availableEntities;
    List<OperatorMetadata> availableOperators;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EntityMetadata {
        SearchEntity entity;
        String entityName;
        List<FieldMetadata> searchableFields;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FieldMetadata {
        String fieldName;
        String fieldType;
        String description;
        List<SearchOperator> applicableOperators;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OperatorMetadata {
        SearchOperator operator;
        String code;
        String description;
    }
}
