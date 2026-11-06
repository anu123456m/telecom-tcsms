package com.amdocs.telecom.model;

import java.time.LocalDateTime;

public class LoginHistory extends BaseEntity {
    private long loginId;
    private UserType userType;
    private Integer customerId;
    private Integer adminId;
    private String usernameAttempted;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private LoginStatus loginStatus;
    private String failureReason;
}