package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.UsageRecord;

import java.time.LocalDate;
import java.util.List;

public interface UsageDAO {
    void insertBatch(List<UsageRecord> records);
    List<UsageRecord> findAllDetailed();
    List<UsageRecord> findByCustomer(int customerId);
    double sumCharges(int subscriptionId, LocalDate from, LocalDate to);
    int countInRange(int subscriptionId, LocalDate from, LocalDate to);
}