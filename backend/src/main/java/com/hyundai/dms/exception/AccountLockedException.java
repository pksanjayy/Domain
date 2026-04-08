package com.hyundai.dms.exception;

import lombok.Getter;

@Getter
public class AccountLockedException extends RuntimeException {
    private final long lockTimeRemainingSeconds;

    public AccountLockedException(String message, long lockTimeRemainingSeconds) {
        super(message);
        this.lockTimeRemainingSeconds = lockTimeRemainingSeconds;
    }
}
