package com.hyundai.dms.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validates VIN: exactly 17 alphanumeric chars.
 */
public class VinValidator implements ConstraintValidator<ValidVin, String> {

    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-Z0-9]{17}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle nulls
        }
        return VIN_PATTERN.matcher(value.toUpperCase()).matches();
    }
}
