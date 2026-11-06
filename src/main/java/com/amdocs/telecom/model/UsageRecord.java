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
}
