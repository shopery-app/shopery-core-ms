package az.shopery.utils.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchOperator {
    EQUALS("equals", "Exact match"),
    NOT_EQUALS("notEquals", "Not equal to"),
    CONTAINS("contains", "Contains text (case-insensitive)"),
    STARTS_WITH("startsWith", "Starts with text"),
    ENDS_WITH("endsWith", "Ends with text"),
    GREATER_THAN("greaterThan", "Greater than"),
    GREATER_THAN_OR_EQUAL("greaterThanOrEqual", "Greater than or equal to"),
    LESS_THAN("lessThan", "Less than"),
    LESS_THAN_OR_EQUAL("lessThanOrEqual", "Less than or equal to"),
    BETWEEN("between", "Between two values"),
    IN("in", "In list of values"),
    NOT_IN("notIn", "Not in list of values"),
    IS_NULL("isNull", "Is null"),
    IS_NOT_NULL("isNotNull", "Is not null");

    private final String code;
    private final String description;
}
