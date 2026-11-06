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
}