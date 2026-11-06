package com.amdocs.telecom.exception;

public class PaymentException extends TcsmsException {
    public PaymentException(String message) {
        super(message);
    }
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}