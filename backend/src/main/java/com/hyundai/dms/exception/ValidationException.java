package com.hyundai.dms.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends DmsException {

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }
}
