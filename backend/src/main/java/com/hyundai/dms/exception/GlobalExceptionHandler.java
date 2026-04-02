package com.hyundai.dms.exception;

import com.hyundai.dms.common.ApiErrorResponse;
import com.hyundai.dms.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DmsException.class)
    public ResponseEntity<ApiResponse<Void>> handleDmsException(DmsException ex, HttpServletRequest request) {
        log.error("[{}] DmsException: {}", getCorrelationId(), ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(getCorrelationId())
                .build();
        return new ResponseEntity<>(ApiResponse.error(error), ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("[{}] Validation error on {}", getCorrelationId(), request.getRequestURI());
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage())
        );
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed")
                .path(request.getRequestURI())
                .correlationId(getCorrelationId())
                .fieldErrors(fieldErrors)
                .build();
        return new ResponseEntity<>(ApiResponse.error(error), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        log.error("[{}] Constraint violation: {}", getCorrelationId(), ex.getMessage());
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv ->
                fieldErrors.put(cv.getPropertyPath().toString(), cv.getMessage())
        );
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("CONSTRAINT_VIOLATION")
                .message("Constraint violation")
                .path(request.getRequestURI())
                .correlationId(getCorrelationId())
                .fieldErrors(fieldErrors)
                .build();
        return new ResponseEntity<>(ApiResponse.error(error), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        log.warn("[{}] Access denied: {}", getCorrelationId(), request.getRequestURI());
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .errorCode("ACCESS_DENIED")
                .message("You do not have permission to access this resource")
                .path(request.getRequestURI())
                .correlationId(getCorrelationId())
                .build();
        return new ResponseEntity<>(ApiResponse.error(error), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        log.warn("[{}] Authentication failed: {}", getCorrelationId(), ex.getMessage());
        String message = "Authentication failed";
        if (ex instanceof BadCredentialsException) {
            message = "Invalid username or password";
        } else if (ex instanceof LockedException) {
            message = "Account is locked. Please try again later";
        }
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .errorCode("AUTHENTICATION_FAILED")
                .message(message)
                .path(request.getRequestURI())
                .correlationId(getCorrelationId())
                .build();
        return new ResponseEntity<>(ApiResponse.error(error), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("[{}] Unhandled exception on {}: {}", getCorrelationId(), request.getRequestURI(), ex.getMessage(), ex);
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode("INTERNAL_ERROR")
                .message("500 Error: " + ex.getMessage() + " | " + ex.getClass().getName())
                .path(request.getRequestURI())
                .correlationId(getCorrelationId())
                .build();
        return new ResponseEntity<>(ApiResponse.error(error), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getCorrelationId() {
        return MDC.get("correlationId");
    }
}
