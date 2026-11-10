package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.Payment;

import java.sql.Connection;
import java.util.List;

public interface PaymentDAO {
    int insert(Connection con, Payment p);
    boolean hasSuccessfulPayment(Connection con, int billId);
    List<Payment> findByCustomer(int customerId);
    List<Payment> findAllDetailed();
}