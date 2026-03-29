package com.hyundai.dms.common.filter;

/**
 * Represents a sort criterion with field name and direction (asc/desc).
 */
public record SortCriteria(
        String field,
        String direction
) {
    public SortCriteria {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("Sort field must not be blank");
        }
        if (direction == null || direction.isBlank()) {
            direction = "asc";
        }
        direction = direction.toLowerCase();
        if (!"asc".equals(direction) && !"desc".equals(direction)) {
            throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc', got: " + direction);
        }
    }
}
