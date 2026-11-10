package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.Bill;
import com.amdocs.telecom.model.BillStatus;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillDAO {
    boolean exists(int subscriptionId, LocalDate billingMonth);
    int insert(Bill bill);
    List<Bill> findByCustomer(int customerId);
    List<Bill> findAllDetailed();
    Optional<Bill> findByIdForUpdate(Connection con, int billId);
    void updateStatus(Connection con, int billId, BillStatus status);
}