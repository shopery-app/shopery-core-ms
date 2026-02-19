package az.shopery.model.dto.request;

import az.shopery.utils.enums.SearchEntity;
import az.shopery.utils.enums.SearchOperator;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
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
public class SearchRequestDto {
    @NotNull(message = "Entity type is required!")
    SearchEntity entity;
    @NotNull(message = "At least one search criterion is required!")
    List<SearchCriterion> criteria;

    Integer page = 0;
    Integer size = 20;
    String sortBy;
    String sortDirection = "desc";

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SearchCriterion {
        @NotBlank(message = "Field name is required!")
        String field;
        @NotNull(message = "Operator is required!")
        SearchOperator operator;
        Object value;
        Object valueFrom;
        Object valueTo;

        @AssertTrue(message = "BETWEEN operator requires both 'valueFrom' and 'valueTo'")
        boolean validBetween;

        @AssertTrue(message = "This operator requires a 'value' field")
        boolean validValue;

        @PostConstruct
        void validate() {
            validBetween = Objects.isNull(operator)
                    || !operator.equals(SearchOperator.BETWEEN)
                    || (Objects.nonNull(valueFrom) && Objects.nonNull(valueTo));

            validValue = Objects.isNull(operator)
                    || operator.equals(SearchOperator.IS_NULL)
                    || operator.equals(SearchOperator.IS_NOT_NULL)
                    || operator.equals(SearchOperator.BETWEEN)
                    || Objects.nonNull(value);
        }
    }
}