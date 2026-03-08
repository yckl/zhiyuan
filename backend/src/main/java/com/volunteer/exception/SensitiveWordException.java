package com.volunteer.exception;

/**
 * 敏感词异常
 */
public class SensitiveWordException extends RuntimeException {
    public SensitiveWordException(String message) {
        super(message);
    }
}
