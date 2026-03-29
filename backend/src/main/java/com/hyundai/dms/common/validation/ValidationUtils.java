package com.hyundai.dms.common.validation;

import com.hyundai.dms.exception.ValidationException;

import java.util.Collection;

/**
 * Programmatic validation guard checks for use in service methods.
 * These complement annotation-based validation by providing imperative validation
 * for business logic that can't be expressed via annotations alone.
 *
 * Client-side validation mirrors these rules using Angular Reactive Forms —
 * field lengths, patterns, and required fields are duplicated in the frontend in Phase 6.
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Utility class — do not instantiate
    }

    /**
     * Asserts that a string value is non-null and non-empty.
     */
    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be empty");
        }
    }

    /**
     * Asserts that a collection is non-null and non-empty.
     */
    public static void requireNonEmpty(Collection<?> value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new ValidationException(fieldName + " must not be empty");
        }
    }

    /**
     * Asserts that a numeric value is positive (> 0).
     */
    public static void requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new ValidationException(fieldName + " must be positive, got: " + value);
        }
    }

    /**
     * Asserts that a numeric value is positive (> 0).
     */
    public static void requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new ValidationException(fieldName + " must be positive, got: " + value);
        }
    }
}
