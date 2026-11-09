package com.amdocs.telecom.model;

public class TelecomPlan extends BaseEntity {
    private int planId;
    private String planCode;
    private String planName;
    private PlanType planType;
    private double monthlyRental;
    private double dataAllowanceGb;
    private int voiceMinutes;      // -1 = unlimited
    private int smsAllowance;      // -1 = unlimited
    private int validityDays;
    private boolean internationalRoaming;
    private boolean restrictedUpgrade;
    private PlanStatus status;

    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }

    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }

    public double getMonthlyRental() { return monthlyRental; }
    public void setMonthlyRental(double monthlyRental) { this.monthlyRental = monthlyRental; }

    public double getDataAllowanceGb() { return dataAllowanceGb; }
    public void setDataAllowanceGb(double dataAllowanceGb) { this.dataAllowanceGb = dataAllowanceGb; }

    public int getVoiceMinutes() { return voiceMinutes; }
    public void setVoiceMinutes(int voiceMinutes) { this.voiceMinutes = voiceMinutes; }

    public int getSmsAllowance() { return smsAllowance; }
    public void setSmsAllowance(int smsAllowance) { this.smsAllowance = smsAllowance; }

    public int getValidityDays() { return validityDays; }
    public void setValidityDays(int validityDays) { this.validityDays = validityDays; }

    public boolean isInternationalRoaming() { return internationalRoaming; }
    public void setInternationalRoaming(boolean internationalRoaming) { this.internationalRoaming = internationalRoaming; }

    public boolean isRestrictedUpgrade() { return restrictedUpgrade; }
    public void setRestrictedUpgrade(boolean restrictedUpgrade) { this.restrictedUpgrade = restrictedUpgrade; }

    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }
}