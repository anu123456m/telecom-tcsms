package com.amdocs.telecom.model;

import java.time.LocalDateTime;

public class Notification extends BaseEntity {
    private long notificationId;
    private int customerId;
    private NotificationType notificationType;
    private String message;
    private NotificationStatus status;
    private LocalDateTime sentAt;
}