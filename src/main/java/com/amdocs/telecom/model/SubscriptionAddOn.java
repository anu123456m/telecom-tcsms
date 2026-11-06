package com.amdocs.telecom.model;

import java.time.LocalDateTime;

public class SubscriptionAddOn extends BaseEntity {
    private int subAddonId;
    private int subscriptionId;
    private int addOnId;
    private LocalDateTime activatedOn;
    private LocalDateTime deactivatedOn;
    private PlanStatus status;
}