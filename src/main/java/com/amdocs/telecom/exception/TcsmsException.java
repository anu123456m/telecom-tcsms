package com.amdocs.telecom.exception;

public class TcsmsException extends RuntimeException {
    public TcsmsException(String message) {
        super(message);
    }
    public TcsmsException(String message, Throwable cause) {
        super(message, cause);
    }
}