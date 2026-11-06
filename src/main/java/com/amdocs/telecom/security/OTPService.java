package com.amdocs.telecom.security;

import java.security.SecureRandom;
import java.time.LocalDateTime;

public class OTPService {

    public static final int VALIDITY_MINUTES = 5;
    private final SecureRandom random = new SecureRandom();

    /** 6-digit code, always exactly 6 digits (100000-999999). */
    public String generateOtp() {
        return String.valueOf(100000 + random.nextInt(900000));
    }

    public LocalDateTime expiryFromNow() {
        return LocalDateTime.now().plusMinutes(VALIDITY_MINUTES);
    }

    public boolean isExpired(LocalDateTime expiresAt) {
        return expiresAt == null || LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean matches(String expected, String input) {
        return expected != null && input != null && expected.equals(input.trim());
    }
}