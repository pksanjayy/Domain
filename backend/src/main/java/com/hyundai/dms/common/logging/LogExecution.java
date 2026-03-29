package com.hyundai.dms.common.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for automatic execution logging.
 * When applied, {@link LogExecutionAspect} will log:
 * - Method entry with arguments (sensitive fields masked)
 * - Method exit with return type name and duration
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecution {
}
