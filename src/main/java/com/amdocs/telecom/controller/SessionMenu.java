package com.amdocs.telecom.controller;

import com.amdocs.telecom.dao.LoginHistoryDAO;
import com.amdocs.telecom.dto.LoginSession;
import com.amdocs.telecom.model.LoginHistory;
import com.amdocs.telecom.model.UserType;
import com.amdocs.telecom.service.AuthenticationService;
import com.amdocs.telecom.util.InputHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SessionMenu {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    private final AuthenticationService authService;
    private final LoginHistoryDAO historyDao;
    private final PlanMenu planMenu;
    private final SubscriptionMenu subscriptionMenu;
    private final UsageBillingMenu usageBillingMenu;
    private final PaymentMenu paymentMenu;

    public SessionMenu(AuthenticationService authService, LoginHistoryDAO historyDao, PlanMenu planMenu,
                       SubscriptionMenu subscriptionMenu, UsageBillingMenu usageBillingMenu,
                       PaymentMenu paymentMenu) {
        this.authService = authService;
        this.historyDao = historyDao;
        this.planMenu = planMenu;
        this.subscriptionMenu = subscriptionMenu;
        this.usageBillingMenu = usageBillingMenu;
        this.paymentMenu = paymentMenu;
    }

    public void show(LoginSession session) {
        boolean admin = session.getUserType() == UserType.ADMIN;

        System.out.println("\n====================================================");
        System.out.println(" Welcome, " + session.getDisplayName() + "  [" + session.getRole() + "]");
        System.out.println(" Last login: " + formatPrevious(session.getPreviousLogin()));
        System.out.println("====================================================");

        boolean active = true;
        while (active) {
            System.out.println("\n1. Browse and compare plans");
            System.out.println("2. My subscriptions");
            System.out.println("3. " + (admin ? "Admin: usage and billing tools" : "My usage and bills"));
            System.out.println("4. " + (admin ? "Admin: view all payments" : "Payments"));
            System.out.println("5. View my login history");
            System.out.println("6. Logout");
            int choice = InputHelper.readInt("Enter choice: ");
            switch (choice) {
                case 1:
                    planMenu.show();
                    break;
                case 2:
                    if (admin) {
                        System.out.println("Subscriptions are available to customers only.");
                    } else {
                        subscriptionMenu.show(session);
                    }
                    break;
                case 3:
                    if (admin) {
                        usageBillingMenu.showAdmin();
                    } else {
                        usageBillingMenu.showCustomer(session);
                    }
                    break;
                case 4:
                    if (admin) {
                        paymentMenu.showAdmin();
                    } else {
                        paymentMenu.showCustomer(session);
                    }
                    break;
                case 5:
                    showHistory(session);
                    break;
                case 6:
                    authService.logout(session);
                    System.out.println("Logged out. See you soon!");
                    active = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void showHistory(LoginSession session) {
        List<LoginHistory> list = historyDao.findRecent(session.getUserType(), session.getUserId(), 5);
        System.out.println("\nRecent login activity:");
        list.forEach(h -> System.out.printf("  %-22s %-8s %s%n",
                FMT.format(h.getLoginTime()),
                h.getLoginStatus(),
                h.getFailureReason() == null ? "" : h.getFailureReason()));
    }

    private String formatPrevious(LocalDateTime previous) {
        return previous == null ? "this is your first login" : FMT.format(previous);
    }
}