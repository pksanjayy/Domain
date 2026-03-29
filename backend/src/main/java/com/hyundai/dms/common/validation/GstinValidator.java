package com.hyundai.dms.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validates GSTIN: 15-character Indian GST Identification Number.
 * Pattern: 2 digits (state code) + 5 alpha (PAN begin) + 4 digits (PAN middle) + 1 alpha (PAN end)
 *          + 1 alphanumeric (entity) + Z + 1 alphanumeric (checksum)
 */
public class GstinValidator implements ConstraintValidator<ValidGstin, String> {

    private static final Pattern GSTIN_PATTERN = Pattern.compile(
            "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]Z[0-9A-Z]$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return GSTIN_PATTERN.matcher(value.toUpperCase()).matches();
    }
}
