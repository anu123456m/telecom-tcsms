package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.PasswordResetOtp;

import java.util.Optional;

public interface OtpDAO {
    int insert(PasswordResetOtp otp);
    Optional<PasswordResetOtp> findLatestUnused(int customerId);
    void markUsed(int otpId);
    void invalidateAll(int customerId);
}