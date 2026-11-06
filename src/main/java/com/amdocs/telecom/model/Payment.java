package com.amdocs.telecom.model;

import java.time.LocalDateTime;

public class Payment extends BaseEntity {
    private int paymentId;
    private String transactionReference;
    private int billId;
    private int customerId;
    private double amount;
    private PaymentMode paymentMode;
    private LocalDateTime paymentDate;
    private PaymentStatus paymentStatus;
}