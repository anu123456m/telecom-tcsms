package com.amdocs.telecom.model;

import java.time.LocalDateTime;

public class Complaint extends BaseEntity {
    private int complaintId;
    private String complaintNumber;
    private int customerId;
    private Integer subscriptionId;
    private ComplaintCategory category;
    private String description;
    private ComplaintPriority priority;
    private LocalDateTime createdDate;
    private ComplaintStatus status;
    private String resolution;
    private Integer resolvedBy;
    private LocalDateTime resolvedDate;
}