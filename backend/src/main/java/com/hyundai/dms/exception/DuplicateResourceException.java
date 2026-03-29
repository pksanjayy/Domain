package com.hyundai.dms.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends DmsException {

    public DuplicateResourceException(String resource, String field, Object value) {
        super(
            String.format("%s already exists with %s: '%s'", resource, field, value),
            "DUPLICATE_RESOURCE",
            HttpStatus.CONFLICT
        );
    }
}
