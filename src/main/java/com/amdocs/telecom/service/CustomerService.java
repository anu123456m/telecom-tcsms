package com.amdocs.telecom.service;

import com.amdocs.telecom.model.Customer;

public interface CustomerService {
    Customer register(Customer customer, String password, String confirmPassword);
}