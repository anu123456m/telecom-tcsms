package com.amdocs.telecom.service;

import com.amdocs.telecom.dto.LoginSession;

public interface AuthenticationService {
    LoginSession loginCustomer(String username, String password);
    LoginSession loginAdmin(String username, String password);
    void logout(LoginSession session);

    /** Creates and stores an OTP, and returns it (the console prints it as a simulated SMS). */
    String requestPasswordReset(String email);
    void resetPassword(String email, String otp, String newPassword);
}