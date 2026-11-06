package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.Customer;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CustomerDAO {
    int insert(Customer customer);
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobileNumber);
    void updateLoginState(int customerId, int failedAttempts, LocalDateTime lockedUntil, LocalDateTime lastLogin);
    void updatePassword(int customerId, String passwordHash);
    String nextCustomerNumber();
}