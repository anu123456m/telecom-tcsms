package com.amdocs.telecom.model;

public class SIMCard extends BaseEntity {
    private int simId;
    private String simNumber;
    private SimType simType;
    private SimStatus simStatus;

    public int getSimId() { return simId; }
    public void setSimId(int simId) { this.simId = simId; }
    public String getSimNumber() { return simNumber; }
    public void setSimNumber(String simNumber) { this.simNumber = simNumber; }
    public SimType getSimType() { return simType; }
    public void setSimType(SimType simType) { this.simType = simType; }
    public SimStatus getSimStatus() { return simStatus; }
    public void setSimStatus(SimStatus simStatus) { this.simStatus = simStatus; }
}