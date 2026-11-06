package com.amdocs.telecom.main;

import com.amdocs.telecom.controller.MainMenu;
import com.amdocs.telecom.controller.SessionMenu;
import com.amdocs.telecom.dao.AdministratorDAO;
import com.amdocs.telecom.dao.CustomerDAO;
import com.amdocs.telecom.dao.LoginHistoryDAO;
import com.amdocs.telecom.dao.OtpDAO;
import com.amdocs.telecom.dao.impl.AdministratorDAOImpl;
import com.amdocs.telecom.dao.impl.CustomerDAOImpl;
import com.amdocs.telecom.dao.impl.LoginHistoryDAOImpl;
import com.amdocs.telecom.dao.impl.OtpDAOImpl;
import com.amdocs.telecom.service.AuthenticationService;
import com.amdocs.telecom.service.CustomerService;
import com.amdocs.telecom.service.impl.AuthenticationServiceImpl;
import com.amdocs.telecom.service.impl.CustomerServiceImpl;
import com.amdocs.telecom.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try (Connection con = DBConnection.getInstance().getConnection()) {
            if (!con.isValid(2)) {
                System.out.println("Database connection is not valid.");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Cannot connect to the database: " + e.getMessage());
            return;
        }

        CustomerDAO customerDao = new CustomerDAOImpl();
        AdministratorDAO adminDao = new AdministratorDAOImpl();
        LoginHistoryDAO historyDao = new LoginHistoryDAOImpl();
        OtpDAO otpDao = new OtpDAOImpl();

        AuthenticationService authService =
                new AuthenticationServiceImpl(customerDao, adminDao, historyDao, otpDao);
        CustomerService customerService = new CustomerServiceImpl(customerDao);

        new MainMenu(authService, customerService, new SessionMenu(authService, historyDao)).start();
    }
}