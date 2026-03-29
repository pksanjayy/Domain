package com.hyundai.dms.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validates a 10-digit Indian mobile number (starting with 6-9).
 */
public class MobileValidator implements ConstraintValidator<ValidMobile, String> {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return MOBILE_PATTERN.matcher(value).matches();
    }
}
