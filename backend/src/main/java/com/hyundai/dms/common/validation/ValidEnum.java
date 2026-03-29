package com.hyundai.dms.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a string value matches one of the names in the specified enum class.
 * Usage: @ValidEnum(enumClass = RoleName.class)
 */
@Documented
@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {
    Class<? extends Enum<?>> enumClass();
    String message() default "Value must match one of the enum constants";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
