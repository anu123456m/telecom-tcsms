package com.amdocs.telecom.main;

import com.amdocs.telecom.dao.AdministratorDAO;
import com.amdocs.telecom.dao.CustomerDAO;
import com.amdocs.telecom.dao.impl.AdministratorDAOImpl;
import com.amdocs.telecom.dao.impl.CustomerDAOImpl;
import com.amdocs.telecom.model.AccountStatus;
import com.amdocs.telecom.model.AdminRole;
import com.amdocs.telecom.model.Administrator;
import com.amdocs.telecom.model.Customer;
import com.amdocs.telecom.security.PasswordUtil;

import java.time.LocalDate;

public class DataSeeder {

    public static void main(String[] args) {
        AdministratorDAO adminDao = new AdministratorDAOImpl();
        CustomerDAO customerDao = new CustomerDAOImpl();

        if (!adminDao.existsByUsername("admin")) {
            Administrator a = new Administrator();
            a.setUsername("admin");
            a.setPasswordHash(PasswordUtil.hash("Admin@123"));
            a.setFullName("System Administrator");
            a.setEmail("admin@tcsms.com");
            a.setRole(AdminRole.SUPER_ADMIN);
            a.setStatus(AccountStatus.ACTIVE);
            adminDao.insert(a);
            System.out.println("Admin created  -> admin / Admin@123");
        } else {
            System.out.println("Admin already exists");
        }

        seedCustomer(customerDao, "Arjun", "Mehta", LocalDate.of(1995, 4, 12),
                "arjun.mehta@example.com", "+91-9876543210", "12 Marine Drive", "Mumbai", "India", "arjun");
        seedCustomer(customerDao, "Sarah", "Wilson", LocalDate.of(1992, 9, 3),
                "sarah.wilson@example.com", "+44-7700900123", "8 Baker Street", "London", "UK", "sarah");
        seedCustomer(customerDao, "Omar", "Hassan", LocalDate.of(1990, 1, 25),
                "omar.hassan@example.com", "+966-501234567", "21 King Fahd Road", "Riyadh", "Saudi Arabia", "omar");
    }

    private static void seedCustomer(CustomerDAO dao, String first, String last, LocalDate dob,
                                     String email, String mobile, String address,
                                     String city, String country, String username) {
        if (dao.existsByUsername(username)) {
            System.out.println("Customer '" + username + "' already exists");
            return;
        }
        Customer c = new Customer();
        c.setCustomerNumber(dao.nextCustomerNumber());
        c.setFirstName(first);
        c.setLastName(last);
        c.setDateOfBirth(dob);
        c.setEmail(email);
        c.setMobileNumber(mobile);
        c.setAddress(address);
        c.setCity(city);
        c.setCountry(country);
        c.setUsername(username);
        c.setPasswordHash(PasswordUtil.hash("Test@1234"));
        c.setAccountStatus(AccountStatus.ACTIVE);
        dao.insert(c);
        System.out.println("Customer created -> " + username + " / Test@1234 (" + c.getCustomerNumber() + ")");
    }
}