package com.amdocs.telecom.dto;

import com.amdocs.telecom.model.UserType;

import java.time.LocalDateTime;

public class LoginSession {

    private final UserType userType;
    private final int userId;
    private final String username;
    private final String displayName;
    private final String role;
    private final long loginId;
    private final LocalDateTime previousLogin;

    public LoginSession(UserType userType, int userId, String username, String displayName,
                        String role, long loginId, LocalDateTime previousLogin) {
        this.userType = userType;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.loginId = loginId;
        this.previousLogin = previousLogin;
    }

    public UserType getUserType() { return userType; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getRole() { return role; }
    public long getLoginId() { return loginId; }
    public LocalDateTime getPreviousLogin() { return previousLogin; }
}