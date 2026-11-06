package com.amdocs.telecom.model;

import java.time.LocalDateTime;

public class SubscriptionHistory extends BaseEntity {
    private long historyId;
    private int subscriptionId;
    private Integer oldPlanId;
    private int newPlanId;
    private LocalDateTime changeDate;
    private String changeReason;
    private String changedBy;
}
