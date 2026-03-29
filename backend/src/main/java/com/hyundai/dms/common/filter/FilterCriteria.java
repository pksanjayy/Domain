package com.hyundai.dms.common.filter;

import java.util.List;

/**
 * Represents a single filter criterion for JPA Specification queries.
 * Supported operators: eq, neq, like, gt, gte, lt, lte, in, between, isNull, isNotNull
 */
public record FilterCriteria(
        String field,
        String operator,
        Object value
) {
    public static final List<String> SUPPORTED_OPERATORS = List.of(
            "eq", "equal", "neq", "not_equal", "like", "gt", "greater_than", 
            "gte", "greater_than_or_equal", "lt", "less_than", "lte", 
            "less_than_or_equal", "in", "between", "isNull", "isNotNull", "not_null"
    );

    public FilterCriteria {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("Filter field must not be blank");
        }
        if (operator == null || SUPPORTED_OPERATORS.stream()
                .noneMatch(op -> op.equalsIgnoreCase(operator))) {
            throw new IllegalArgumentException("Unsupported filter operator: " + operator
                    + ". Supported: " + SUPPORTED_OPERATORS);
        }
    }
}
