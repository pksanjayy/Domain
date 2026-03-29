package com.hyundai.dms.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DmsException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public DmsException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public DmsException(String message, String errorCode, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }
}
