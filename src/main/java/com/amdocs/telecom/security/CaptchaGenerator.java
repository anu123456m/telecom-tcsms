package com.amdocs.telecom.security;

import java.security.SecureRandom;

public final class CaptchaGenerator {

    // no 0/O or 1/I so humans don't get confused
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private CaptchaGenerator() {
    }

    public static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public static boolean verify(String expected, String input) {
        return expected != null && input != null && expected.equalsIgnoreCase(input.trim());
    }
}