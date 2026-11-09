package com.amdocs.telecom.model;

public class AddOn extends BaseEntity {
    private int addOnId;
    private String addOnCode;
    private String addOnName;
    private String description;
    private double monthlyCharge;
    private PlanStatus status;

    public int getAddOnId() { return addOnId; }
    public void setAddOnId(int addOnId) { this.addOnId = addOnId; }
    public String getAddOnCode() { return addOnCode; }
    public void setAddOnCode(String addOnCode) { this.addOnCode = addOnCode; }
    public String getAddOnName() { return addOnName; }
    public void setAddOnName(String addOnName) { this.addOnName = addOnName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getMonthlyCharge() { return monthlyCharge; }
    public void setMonthlyCharge(double monthlyCharge) { this.monthlyCharge = monthlyCharge; }
    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }
}