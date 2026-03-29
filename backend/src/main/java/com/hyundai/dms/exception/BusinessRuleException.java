package com.hyundai.dms.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends DmsException {

    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
