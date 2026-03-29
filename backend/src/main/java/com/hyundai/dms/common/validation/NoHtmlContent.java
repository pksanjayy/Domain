package com.hyundai.dms.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a string field does not contain HTML/script injection.
 * Rejects strings containing &lt;script&gt;, &lt;img, onerror=, onclick=, javascript: etc.
 */
@Documented
@Constraint(validatedBy = NoHtmlContentValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoHtmlContent {
    String message() default "Field must not contain HTML or script content";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
