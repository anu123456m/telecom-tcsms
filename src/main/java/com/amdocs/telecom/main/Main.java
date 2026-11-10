package com.amdocs.telecom.main;

import com.amdocs.telecom.controller.MainMenu;
import com.amdocs.telecom.controller.PaymentMenu;
import com.amdocs.telecom.controller.PlanMenu;
import com.amdocs.telecom.controller.SessionMenu;
import com.amdocs.telecom.controller.SubscriptionMenu;
import com.amdocs.telecom.controller.UsageBillingMenu;
import com.amdocs.telecom.dao.AddOnDAO;
import com.amdocs.telecom.dao.AdministratorDAO;
import com.amdocs.telecom.dao.AuditDAO;
import com.amdocs.telecom.dao.BillDAO;
import com.amdocs.telecom.dao.CustomerDAO;
import com.amdocs.telecom.dao.LoginHistoryDAO;
import com.amdocs.telecom.dao.NotificationDAO;
import com.amdocs.telecom.dao.OtpDAO;
import com.amdocs.telecom.dao.PaymentDAO;
import com.amdocs.telecom.dao.PlanDAO;
import com.amdocs.telecom.dao.SimDAO;
import com.amdocs.telecom.dao.SubscriptionDAO;
import com.amdocs.telecom.dao.SubscriptionHistoryDAO;
import com.amdocs.telecom.dao.UsageDAO;
import com.amdocs.telecom.dao.impl.AddOnDAOImpl;
import com.amdocs.telecom.dao.impl.AdministratorDAOImpl;
import com.amdocs.telecom.dao.impl.AuditDAOImpl;
import com.amdocs.telecom.dao.impl.BillDAOImpl;
import com.amdocs.telecom.dao.impl.CustomerDAOImpl;
import com.amdocs.telecom.dao.impl.LoginHistoryDAOImpl;
import com.amdocs.telecom.dao.impl.NotificationDAOImpl;
import com.amdocs.telecom.dao.impl.OtpDAOImpl;
import com.amdocs.telecom.dao.impl.PaymentDAOImpl;
import com.amdocs.telecom.dao.impl.PlanDAOImpl;
import com.amdocs.telecom.dao.impl.SimDAOImpl;
import com.amdocs.telecom.dao.impl.SubscriptionDAOImpl;
import com.amdocs.telecom.dao.impl.SubscriptionHistoryDAOImpl;
import com.amdocs.telecom.dao.impl.UsageDAOImpl;
import com.amdocs.telecom.service.AuthenticationService;
import com.amdocs.telecom.service.BillingService;
import com.amdocs.telecom.service.CustomerService;
import com.amdocs.telecom.service.DiscountStrategy;
import com.amdocs.telecom.service.PaymentService;
import com.amdocs.telecom.service.PlanService;
import com.amdocs.telecom.service.SubscriptionService;
import com.amdocs.telecom.service.UsageService;
import com.amdocs.telecom.service.impl.AuthenticationServiceImpl;
import com.amdocs.telecom.service.impl.BillingServiceImpl;
import com.amdocs.telecom.service.impl.CustomerServiceImpl;
import com.amdocs.telecom.service.impl.PaymentServiceImpl;
import com.amdocs.telecom.service.impl.PlanServiceImpl;
import com.amdocs.telecom.service.impl.PostpaidDiscountStrategy;
import com.amdocs.telecom.service.impl.SubscriptionServiceImpl;
import com.amdocs.telecom.service.impl.UsageServiceImpl;
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
        PlanDAO planDao = new PlanDAOImpl();
        SimDAO simDao = new SimDAOImpl();
        SubscriptionDAO subscriptionDao = new SubscriptionDAOImpl();
        SubscriptionHistoryDAO subHistoryDao = new SubscriptionHistoryDAOImpl();
        AddOnDAO addOnDao = new AddOnDAOImpl();
        UsageDAO usageDao = new UsageDAOImpl();
        BillDAO billDao = new BillDAOImpl();
        PaymentDAO paymentDao = new PaymentDAOImpl();
        AuditDAO auditDao = new AuditDAOImpl();
        NotificationDAO notificationDao = new NotificationDAOImpl();

        DiscountStrategy discountStrategy = new PostpaidDiscountStrategy();

        AuthenticationService authService =
                new AuthenticationServiceImpl(customerDao, adminDao, historyDao, otpDao);
        CustomerService customerService = new CustomerServiceImpl(customerDao);
        PlanService planService = new PlanServiceImpl(planDao);
        SubscriptionService subscriptionService =
                new SubscriptionServiceImpl(planDao, simDao, subscriptionDao, subHistoryDao, addOnDao);
        UsageService usageService = new UsageServiceImpl(usageDao, subscriptionDao, planDao);
        BillingService billingService =
                new BillingServiceImpl(billDao, usageDao, subscriptionDao, planDao, discountStrategy);
        PaymentService paymentService =
                new PaymentServiceImpl(billDao, paymentDao, auditDao, notificationDao);

        PlanMenu planMenu = new PlanMenu(planService);
        SubscriptionMenu subscriptionMenu = new SubscriptionMenu(subscriptionService, planService);
        UsageBillingMenu usageBillingMenu = new UsageBillingMenu(usageService, billingService);
        PaymentMenu paymentMenu = new PaymentMenu(paymentService);
        SessionMenu sessionMenu = new SessionMenu(authService, historyDao, planMenu, subscriptionMenu,
                usageBillingMenu, paymentMenu);

        new MainMenu(authService, customerService, sessionMenu).start();
    }
}