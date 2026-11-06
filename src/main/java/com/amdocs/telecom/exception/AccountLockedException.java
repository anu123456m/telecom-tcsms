package com.amdocs.telecom.exception;

public class AccountLockedException extends TcsmsException {
    public AccountLockedException(String message) {
        super(message);
    }
}