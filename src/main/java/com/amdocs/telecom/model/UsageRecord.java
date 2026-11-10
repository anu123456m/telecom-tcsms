package com.amdocs.telecom.model;

import java.time.LocalDate;

public class UsageRecord extends BaseEntity {
    private long usageId;
    private int subscriptionId;
    private LocalDate usageDate;
    private UsageType usageType;
    private double quantity;
    private UsageUnit unit;
    private double charge;
    private boolean processed;
    private int customerId;        // filled by a JOIN
    private String customerName;   // filled by a JOIN

    public long getUsageId() { return usageId; }
    public void setUsageId(long usageId) { this.usageId = usageId; }
    public int getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(int subscriptionId) { this.subscriptionId = subscriptionId; }
    public LocalDate getUsageDate() { return usageDate; }
    public void setUsageDate(LocalDate usageDate) { this.usageDate = usageDate; }
    public UsageType getUsageType() { return usageType; }
    public void setUsageType(UsageType usageType) { this.usageType = usageType; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public UsageUnit getUnit() { return unit; }
    public void setUnit(UsageUnit unit) { this.unit = unit; }
    public double getCharge() { return charge; }
    public void setCharge(double charge) { this.charge = charge; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}