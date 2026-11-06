package com.amdocs.telecom.model;

import java.time.LocalDate;

public class Bill extends BaseEntity {
    private int billId;
    private String billNumber;
    private int subscriptionId;
    private LocalDate billingMonth;
    private double planRental;
    private double usageCharges;
    private double taxAmount;
    private double discount;
    private double totalAmount;
    private LocalDate dueDate;
    private BillStatus billStatus;
}