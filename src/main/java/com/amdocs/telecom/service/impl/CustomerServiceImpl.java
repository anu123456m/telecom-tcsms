package com.amdocs.telecom.service.impl;

import com.amdocs.telecom.dao.CustomerDAO;
import com.amdocs.telecom.exception.DuplicateResourceException;
import com.amdocs.telecom.exception.ValidationException;
import com.amdocs.telecom.model.AccountStatus;
import com.amdocs.telecom.model.Customer;
import com.amdocs.telecom.security.PasswordUtil;
import com.amdocs.telecom.service.CustomerService;
import com.amdocs.telecom.validation.Validators;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerDAO customerDao;

    public CustomerServiceImpl(CustomerDAO customerDao) {
        this.customerDao = customerDao;
    }

    @Override
    public Customer register(Customer c, String password, String confirmPassword) {
        if (c.getEmail() != null) {
            c.setEmail(c.getEmail().trim().toLowerCase());
        }

        List<String> errors = new ArrayList<>();
        check(errors, Validators.NOT_BLANK.test(c.getFirstName()), "First name is required");
        check(errors, Validators.NOT_BLANK.test(c.getLastName()), "Last name is required");
        check(errors, Validators.NOT_BLANK.test(c.getAddress()), "Address is required");
        check(errors, Validators.NOT_BLANK.test(c.getCity()), "City is required");
        check(errors, Validators.NOT_BLANK.test(c.getCountry()), "Country is required");
        check(errors, Validators.VALID_EMAIL.test(c.getEmail()), "Email is not valid");
        check(errors, Validators.VALID_MOBILE.test(c.getMobileNumber()),
                "Mobile number is not valid (example: +91-9876543210)");
        check(errors, Validators.VALID_USERNAME.test(c.getUsername()),
                "Username must be 4-30 characters: letters, digits or underscore");
        if (c.getDateOfBirth() == null) {
            errors.add("Date of birth must be a valid date (yyyy-MM-dd)");
        } else if (!Validators.isAdult(c.getDateOfBirth())) {
            errors.add("You must be at least " + Validators.MIN_AGE + " years old to register");
        }
        check(errors, PasswordUtil.isStrong(password), PasswordUtil.complexityRules());
        check(errors, password != null && password.equals(confirmPassword), "Passwords do not match");

        if (!errors.isEmpty()) {
            throw new ValidationException("Please fix the following:\n - " + String.join("\n - ", errors));
        }

        if (customerDao.existsByEmail(c.getEmail())) {
            throw new DuplicateResourceException("This email is already registered.");
        }
        if (customerDao.existsByMobile(c.getMobileNumber())) {
            throw new DuplicateResourceException("This mobile number is already registered.");
        }
        if (customerDao.existsByUsername(c.getUsername())) {
            throw new DuplicateResourceException("This username is already taken.");
        }

        c.setCustomerNumber(customerDao.nextCustomerNumber());
        c.setPasswordHash(PasswordUtil.hash(password));
        c.setAccountStatus(AccountStatus.ACTIVE);
        c.setRegistrationDate(LocalDate.now());
        c.setCustomerId(customerDao.insert(c));
        return c;
    }

    private void check(List<String> errors, boolean valid, String message) {
        if (!valid) {
            errors.add(message);
        }
    }
}