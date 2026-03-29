package com.hyundai.dms.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Rejects strings containing HTML tags, script tags, event handlers, and javascript: URIs.
 */
public class NoHtmlContentValidator implements ConstraintValidator<NoHtmlContent, String> {

    private static final Pattern HTML_PATTERN = Pattern.compile(
            "(?i)(<\\s*script\\b|<\\s*img\\b|<\\s*iframe\\b|<\\s*object\\b|<\\s*embed\\b|<\\s*link\\b" +
            "|onerror\\s*=|onclick\\s*=|onload\\s*=|onmouseover\\s*=|onfocus\\s*=" +
            "|javascript\\s*:|data\\s*:text/html)"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !HTML_PATTERN.matcher(value).find();
    }
}
