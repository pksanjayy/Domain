package com.hyundai.dms.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends DmsException {

    public AccessDeniedException(String message) {
        super(message, "ACCESS_DENIED", HttpStatus.FORBIDDEN);
    }

    public AccessDeniedException() {
        this("You do not have permission to perform this action");
    }
}
