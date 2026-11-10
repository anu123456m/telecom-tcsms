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
    private String mobileNumber;   // filled by a JOIN
    private int customerId;        // filled by a JOIN
    private String customerName;   // filled by a JOIN

    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }
    public int getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(int subscriptionId) { this.subscriptionId = subscriptionId; }
    public LocalDate getBillingMonth() { return billingMonth; }
    public void setBillingMonth(LocalDate billingMonth) { this.billingMonth = billingMonth; }
    public double getPlanRental() { return planRental; }
    public void setPlanRental(double planRental) { this.planRental = planRental; }
    public double getUsageCharges() { return usageCharges; }
    public void setUsageCharges(double usageCharges) { this.usageCharges = usageCharges; }
    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BillStatus getBillStatus() { return billStatus; }
    public void setBillStatus(BillStatus billStatus) { this.billStatus = billStatus; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}