package com.hyundai.dms.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a VIN (Vehicle Identification Number) is 17 alphanumeric characters.
 */
@Documented
@Constraint(validatedBy = VinValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVin {
    String message() default "VIN must be 17 alphanumeric characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
