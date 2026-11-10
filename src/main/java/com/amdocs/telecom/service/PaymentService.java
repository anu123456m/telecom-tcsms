package com.amdocs.telecom.service;

import com.amdocs.telecom.model.Bill;
import com.amdocs.telecom.model.Payment;
import com.amdocs.telecom.model.PaymentMode;

import java.util.List;

public interface PaymentService {
    List<Bill> getPayableBills(int customerId);
    Payment pay(int customerId, int billId, double amount, PaymentMode mode);
    List<Payment> getPaymentsForCustomer(int customerId);
    List<Payment> getAllPayments();
}