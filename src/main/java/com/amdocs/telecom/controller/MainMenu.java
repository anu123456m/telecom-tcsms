package com.amdocs.telecom.controller;

import com.amdocs.telecom.dto.LoginSession;
import com.amdocs.telecom.exception.TcsmsException;
import com.amdocs.telecom.exception.ValidationException;
import com.amdocs.telecom.model.Customer;
import com.amdocs.telecom.security.CaptchaGenerator;
import com.amdocs.telecom.security.PasswordUtil;
import com.amdocs.telecom.service.AuthenticationService;
import com.amdocs.telecom.service.CustomerService;
import com.amdocs.telecom.util.InputHelper;

public class MainMenu {

    private final AuthenticationService authService;
    private final CustomerService customerService;
    private final SessionMenu sessionMenu;

    public MainMenu(AuthenticationService authService, CustomerService customerService,
                    SessionMenu sessionMenu) {
        this.authService = authService;
        this.customerService = customerService;
        this.sessionMenu = sessionMenu;
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = InputHelper.readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1:
                        customerLogin();
                        break;
                    case 2:
                        register();
                        break;
                    case 3:
                        adminLogin();
                        break;
                    case 4:
                        forgotPassword();
                        break;
                    case 5:
                        System.out.println("Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (TcsmsException e) {
                System.out.println("\n[!] " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n====================================================");
        System.out.println(" TELECOM CUSTOMER MANAGEMENT SYSTEM");
        System.out.println("====================================================");
        System.out.println("1. Customer Login");
        System.out.println("2. New Customer Registration");
        System.out.println("3. Administrator Login");
        System.out.println("4. Forgot Password");
        System.out.println("5. Exit");
    }

    private void customerLogin() {
        System.out.println("\n--- Customer Login ---");
        String username = InputHelper.readLine("Username: ");
        String password = InputHelper.readPassword("Password: ");
        if (!captchaPassed()) {
            return;
        }
        LoginSession session = authService.loginCustomer(username, password);
        sessionMenu.show(session);
    }

    private void adminLogin() {
        System.out.println("\n--- Administrator Login ---");
        String username = InputHelper.readLine("Username: ");
        String password = InputHelper.readPassword("Password: ");
        if (!captchaPassed()) {
            return;
        }
        LoginSession session = authService.loginAdmin(username, password);
        sessionMenu.show(session);
    }

    private void register() {
        System.out.println("\n--- New Customer Registration ---");
        Customer c = new Customer();
        c.setFirstName(InputHelper.readLine("First name: "));
        c.setLastName(InputHelper.readLine("Last name: "));
        c.setDateOfBirth(InputHelper.readDate("Date of birth (yyyy-MM-dd): "));
        c.setEmail(InputHelper.readLine("Email: "));
        c.setMobileNumber(InputHelper.readLine("Mobile number (e.g. +91-9876543210): "));
        c.setAddress(InputHelper.readLine("Address: "));
        c.setCity(InputHelper.readLine("City: "));
        c.setCountry(InputHelper.readLine("Country: "));
        c.setUsername(InputHelper.readLine("Choose a username: "));
        System.out.println(PasswordUtil.complexityRules());
        String password = InputHelper.readPassword("Password: ");
        String confirm = InputHelper.readPassword("Confirm password: ");

        Customer saved = customerService.register(c, password, confirm);
        System.out.println("\nRegistration successful! Your customer number is " + saved.getCustomerNumber());
    }

    private void forgotPassword() {
        System.out.println("\n--- Forgot Password ---");
        String email = InputHelper.readLine("Registered email: ");
        String otp = authService.requestPasswordReset(email);
        System.out.println("[SMS simulation] Your OTP is " + otp + " (valid for 5 minutes)");

        String entered = InputHelper.readLine("Enter OTP: ");
        System.out.println(PasswordUtil.complexityRules());
        String newPassword = InputHelper.readPassword("New password: ");
        String confirm = InputHelper.readPassword("Confirm new password: ");
        if (!newPassword.equals(confirm)) {
            throw new ValidationException("Passwords do not match.");
        }
        authService.resetPassword(email, entered, newPassword);
        System.out.println("Password changed. You can log in now.");
    }

    private boolean captchaPassed() {
        String captcha = CaptchaGenerator.generate();
        System.out.println("CAPTCHA: " + captcha);
        String typed = InputHelper.readLine("Type the CAPTCHA: ");
        if (!CaptchaGenerator.verify(captcha, typed)) {
            System.out.println("[!] CAPTCHA did not match. Please try again.");
            return false;
        }
        return true;
    }
}