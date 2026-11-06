package com.amdocs.telecom.model;

public class AddOn extends BaseEntity {
    private int addOnId;
    private String addOnCode;
    private String addOnName;
    private String description;
    private double monthlyCharge;
    private PlanStatus status;
}