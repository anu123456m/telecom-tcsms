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
    private SimType simType;     // filled by a JOIN with sim_card
    private String planName;     // filled by a JOIN with telecom_plan

    public int getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(int subscriptionId) { this.subscriptionId = subscriptionId; }
    public String getSubscriptionNumber() { return subscriptionNumber; }
    public void setSubscriptionNumber(String subscriptionNumber) { this.subscriptionNumber = subscriptionNumber; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }
    public int getSimId() { return simId; }
    public void setSimId(int simId) { this.simId = simId; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public LocalDate getActivationDate() { return activationDate; }
    public void setActivationDate(LocalDate activationDate) { this.activationDate = activationDate; }
    public PlanType getSubscriptionType() { return subscriptionType; }
    public void setSubscriptionType(PlanType subscriptionType) { this.subscriptionType = subscriptionType; }
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    public SimType getSimType() { return simType; }
    public void setSimType(SimType simType) { this.simType = simType; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
}