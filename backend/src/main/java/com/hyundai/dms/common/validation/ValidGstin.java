package com.hyundai.dms.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a string matches the Indian GSTIN pattern.
 * Format: 2-digit state code + 10-char PAN + 1 entity no + 1 default Z + 1 checksum
 */
@Documented
@Constraint(validatedBy = GstinValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGstin {
    String message() default "Must be a valid 15-character GSTIN";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
