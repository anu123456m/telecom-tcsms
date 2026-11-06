package com.amdocs.telecom.model;

import java.time.LocalDate;

public class MobileSubscription extends BaseEntity {
    private int subscriptionId;
    private String subscriptionNumber;
    private int customerId;
    private int planId;
    private int simId;
    private String mobileNumber;
    private LocalDate activationDate;
    private PlanType subscriptionType;
    private SubscriptionStatus status;
    private SimType simType;       // filled by a JOIN with sim_card, not stored here
}