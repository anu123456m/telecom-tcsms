package com.amdocs.telecom.model;

import java.time.LocalDateTime;

public class SubscriptionAddOn extends BaseEntity {
    private int subAddonId;
    private int subscriptionId;
    private int addOnId;
    private LocalDateTime activatedOn;
    private LocalDateTime deactivatedOn;
    private PlanStatus status;

    public int getSubAddonId() { return subAddonId; }
    public void setSubAddonId(int subAddonId) { this.subAddonId = subAddonId; }
    public int getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(int subscriptionId) { this.subscriptionId = subscriptionId; }
    public int getAddOnId() { return addOnId; }
    public void setAddOnId(int addOnId) { this.addOnId = addOnId; }
    public LocalDateTime getActivatedOn() { return activatedOn; }
    public void setActivatedOn(LocalDateTime activatedOn) { this.activatedOn = activatedOn; }
    public LocalDateTime getDeactivatedOn() { return deactivatedOn; }
    public void setDeactivatedOn(LocalDateTime deactivatedOn) { this.deactivatedOn = deactivatedOn; }
    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }
}