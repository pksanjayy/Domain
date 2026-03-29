package com.hyundai.dms.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AOP aspect that logs method entry and exit for methods annotated with {@link LogExecution}.
 * Masks sensitive fields: password, token, secret, cardNumber, aadhaar, pan.
 */
@Slf4j
@Aspect
@Component
public class LogExecutionAspect {

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "token", "secret", "cardNumber", "aadhaar", "pan"
    );

    @Around("@annotation(com.hyundai.dms.common.logging.LogExecution)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        String maskedArgs = maskArguments(joinPoint.getArgs());
        log.info("[ENTER] {}.{}({})", className, methodName, maskedArgs);

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long durationMs = System.currentTimeMillis() - startTime;

            String returnTypeName = result != null ? result.getClass().getSimpleName() : "void";
            log.info("[EXIT] {}.{} → {} ({}ms)", className, methodName, returnTypeName, durationMs);
            return result;
        } catch (Throwable ex) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("[ERROR] {}.{} threw {} after {}ms: {}",
                    className, methodName, ex.getClass().getSimpleName(), durationMs, ex.getMessage());
            throw ex;
        }
    }

    private String maskArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }

        return Arrays.stream(args)
                .map(this::maskSensitiveFields)
                .collect(Collectors.joining(", "));
    }

    private String maskSensitiveFields(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
            return arg.toString();
        }

        // For complex objects, inspect fields and mask sensitive ones
        StringBuilder sb = new StringBuilder(arg.getClass().getSimpleName()).append("{");
        Field[] fields = arg.getClass().getDeclaredFields();
        boolean first = true;

        for (Field field : fields) {
            if (!first) sb.append(", ");
            first = false;

            field.setAccessible(true);
            String fieldName = field.getName();
            try {
                Object value = field.get(arg);
                if (SENSITIVE_FIELDS.contains(fieldName.toLowerCase())) {
                    sb.append(fieldName).append("=****");
                } else {
                    sb.append(fieldName).append("=").append(value);
                }
            } catch (IllegalAccessException e) {
                sb.append(fieldName).append("=<inaccessible>");
            }
        }

        sb.append("}");
        return sb.toString();
    }
}
