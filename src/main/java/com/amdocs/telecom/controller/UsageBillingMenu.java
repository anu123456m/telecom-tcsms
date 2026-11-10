package com.amdocs.telecom.controller;

import com.amdocs.telecom.dto.LoginSession;
import com.amdocs.telecom.exception.TcsmsException;
import com.amdocs.telecom.exception.ValidationException;
import com.amdocs.telecom.model.Bill;
import com.amdocs.telecom.model.UsageType;
import com.amdocs.telecom.service.BillingService;
import com.amdocs.telecom.service.UsageService;
import com.amdocs.telecom.util.InputHelper;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

public class UsageBillingMenu {

    private final UsageService usageService;
    private final BillingService billingService;

    public UsageBillingMenu(UsageService usageService, BillingService billingService) {
        this.usageService = usageService;
        this.billingService = billingService;
    }

    // ---------- customer ----------

    public void showCustomer(LoginSession session) {
        boolean active = true;
        while (active) {
            System.out.println("\n--- My Usage and Bills ---");
            System.out.println("1. My usage summary");
            System.out.println("2. My bills");
            System.out.println("3. Back");
            int choice = InputHelper.readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1:
                        printUsage(usageService.totalsForCustomer(session.getUserId()));
                        break;
                    case 2:
                        printBills(billingService.getBillsForCustomer(session.getUserId()));
                        break;
                    case 3:
                        active = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (TcsmsException e) {
                System.out.println("\n[!] " + e.getMessage());
            }
        }
    }

    // ---------- administrator ----------

    public void showAdmin() {
        boolean active = true;
        while (active) {
            System.out.println("\n--- Admin: Usage and Billing ---");
            System.out.println("1. Generate sample usage for a month");
            System.out.println("2. Generate bills for a month");
            System.out.println("3. View bills for a month");
            System.out.println("4. Usage analytics");
            System.out.println("5. Back");
            int choice = InputHelper.readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1:
                        generateUsage();
                        break;
                    case 2:
                        generateBills();
                        break;
                    case 3:
                        printBills(billingService.getBillsForMonth(readMonth()));
                        break;
                    case 4:
                        analytics();
                        break;
                    case 5:
                        active = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (TcsmsException e) {
                System.out.println("\n[!] " + e.getMessage());
            }
        }
    }

    private void generateUsage() {
        YearMonth month = readMonth();
        int created = usageService.generateSampleUsage(month);
        System.out.println("\nCreated " + created + " usage records for " + month + ".");
        if (created == 0) {
            System.out.println("(Subscriptions that already have usage for that month are skipped.)");
        }
    }

    private void generateBills() {
        YearMonth month = readMonth();
        int[] result = billingService.generateBillsForMonth(month);
        System.out.println("\nBills generated: " + result[0] + ", skipped: " + result[1]
                + " (already billed, or the subscription had not started).");
    }

    private void analytics() {
        Map<UsageType, Double> totals = usageService.totalsByType();
        if (totals.isEmpty()) {
            System.out.println("\nNo usage recorded yet. Generate sample usage first.");
            return;
        }
        System.out.println("\nTotal usage by type:");
        printUsage(totals);

        System.out.println("\nMonthly usage:");
        usageService.monthlyUsage().forEach((month, byType) -> {
            StringBuilder sb = new StringBuilder("  " + month + ":  ");
            Arrays.stream(UsageType.values())
                    .filter(byType::containsKey)
                    .forEach(t -> sb.append(String.format("%s %.1f %s   ", t, byType.get(t), unitOf(t))));
            System.out.println(sb);
        });

        System.out.println("\nTop data consumers:");
        usageService.topDataConsumers(3).forEach(e ->
                System.out.printf("  %-22s %8.1f GB%n", e.getKey(), e.getValue()));

        DoubleSummaryStatistics stats = usageService.dataSessionStats();
        if (stats.getCount() > 0) {
            System.out.printf("%nData sessions: %d, average %.2f GB, largest %.2f GB%n",
                    stats.getCount(), stats.getAverage(), stats.getMax());
        }
    }

    // ---------- shared ----------

    private YearMonth readMonth() {
        String text = InputHelper.readLine("Month (yyyy-MM, press Enter for the current month): ");
        if (text.isEmpty()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Enter the month like 2026-11.");
        }
    }

    private void printUsage(Map<UsageType, Double> totals) {
        if (totals.isEmpty()) {
            System.out.println("\nNo usage recorded yet.");
            return;
        }
        System.out.println();
        Arrays.stream(UsageType.values())
                .filter(totals::containsKey)
                .forEach(t -> System.out.printf("  %-8s %10.1f %s%n", t, totals.get(t), unitOf(t)));
    }

    private void printBills(List<Bill> bills) {
        if (bills.isEmpty()) {
            System.out.println("\nNo bills found.");
            return;
        }
        System.out.println();
        System.out.printf("%-18s %-15s %-8s %9s %8s %8s %8s %10s %-12s %-10s%n",
                "Bill no.", "Mobile", "Month", "Rental", "Usage", "Tax", "Discount", "Total", "Due", "Status");
        bills.forEach(b -> System.out.printf("%-18s %-15s %-8s %9.2f %8.2f %8.2f %8.2f %10.2f %-12s %-10s%n",
                b.getBillNumber(),
                b.getMobileNumber(),
                YearMonth.from(b.getBillingMonth()),
                b.getPlanRental(),
                b.getUsageCharges(),
                b.getTaxAmount(),
                b.getDiscount(),
                b.getTotalAmount(),
                b.getDueDate(),
                b.getBillStatus()));
    }

    private static String unitOf(UsageType type) {
        switch (type) {
            case DATA:
                return "GB";
            case VOICE:
                return "minutes";
            case SMS:
                return "messages";
            default:
                return "MB";
        }
    }
}