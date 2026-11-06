package com.amdocs.telecom.model;

import java.time.LocalDateTime;

public class PasswordResetOtp extends BaseEntity {
    private int otpId;
    private int customerId;
    private String otpCode;
    private LocalDateTime expiresAt;
    private boolean used;
}