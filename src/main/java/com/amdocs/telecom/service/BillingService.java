package com.amdocs.telecom.service;

import com.amdocs.telecom.model.Bill;

import java.time.YearMonth;
import java.util.List;

public interface BillingService {
    Bill generateBill(int subscriptionId, YearMonth month);

    /** Returns {generated, skipped}. */
    int[] generateBillsForMonth(YearMonth month);

    List<Bill> getBillsForCustomer(int customerId);
    List<Bill> getBillsForMonth(YearMonth month);
}