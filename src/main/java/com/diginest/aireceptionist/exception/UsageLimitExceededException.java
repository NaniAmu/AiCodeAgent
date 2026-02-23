package com.diginest.aireceptionist.exception;

public class UsageLimitExceededException extends RuntimeException {
    public UsageLimitExceededException(String message) {
        super(message);
    }
}
