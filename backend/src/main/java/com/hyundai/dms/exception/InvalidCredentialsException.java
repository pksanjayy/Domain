package com.hyundai.dms.exception;

import lombok.Getter;

@Getter
public class InvalidCredentialsException extends RuntimeException {
    private final int remainingAttempts;

    public InvalidCredentialsException(String message, int remainingAttempts) {
        super(message);
        this.remainingAttempts = remainingAttempts;
    }
}
