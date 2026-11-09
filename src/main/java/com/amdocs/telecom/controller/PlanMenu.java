package com.amdocs.telecom.controller;

import com.amdocs.telecom.exception.TcsmsException;
import com.amdocs.telecom.model.TelecomPlan;
import com.amdocs.telecom.service.PlanService;
import com.amdocs.telecom.util.InputHelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class PlanMenu {

    private final PlanService planService;

    public PlanMenu(PlanService planService) {
        this.planService = planService;
    }

    public void show() {
        boolean active = true;
        while (active) {
            System.out.println("\n--- Plan Catalogue ---");
            System.out.println("1. View all plans");
            System.out.println("2. Search by name or code");
            System.out.println("3. Filter by price range");
            System.out.println("4. Filter by minimum data (GB)");
            System.out.println("5. Sort by price");
            System.out.println("6. Compare plans");
            System.out.println("7. Back");
            int choice = InputHelper.readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1:
                        printPlans(planService.getActivePlans());
                        break;
                    case 2:
                        printPlans(planService.searchByName(InputHelper.readLine("Search text: ")));
                        break;
                    case 3:
                        double min = InputHelper.readDouble("Minimum monthly rental: ");
                        double max = InputHelper.readDouble("Maximum monthly rental: ");
                        printPlans(planService.filterByPriceRange(min, max));
                        break;
                    case 4:
                        printPlans(planService.filterByMinData(InputHelper.readDouble("Minimum data (GB): ")));
                        break;
                    case 5:
                        System.out.println("1. Low to high   2. High to low");
                        boolean ascending = InputHelper.readInt("Enter choice: ") == 1;
                        printPlans(planService.sortByPrice(ascending));
                        break;
                    case 6:
                        compare();
                        break;
                    case 7:
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

    private void compare() {
        String input = InputHelper.readLine("Enter 2 or 3 plan codes separated by commas (e.g. PLAN-101,PLAN-102): ");
        List<TelecomPlan> plans = planService.compare(Arrays.asList(input.split(",")));

        System.out.println();
        row("", plans, TelecomPlan::getPlanCode);
        row("Name", plans, TelecomPlan::getPlanName);
        row("Type", plans, p -> p.getPlanType().name());
        row("Monthly rental", plans, p -> String.format("Rs.%.2f", p.getMonthlyRental()));
        row("Data", plans, p -> String.format("%.0f GB", p.getDataAllowanceGb()));
        row("Voice", plans, p -> limit(p.getVoiceMinutes(), " min"));
        row("SMS", plans, p -> limit(p.getSmsAllowance(), ""));
        row("Validity", plans, p -> p.getValidityDays() + " days");
        row("Intl roaming", plans, p -> p.isInternationalRoaming() ? "Yes" : "No");

        plans.stream()
                .filter(p -> p.getDataAllowanceGb() > 0)
                .min(Comparator.comparingDouble(p -> p.getMonthlyRental() / p.getDataAllowanceGb()))
                .ifPresent(p -> System.out.println("\nLowest cost per GB: " + p.getPlanCode() + " - " + p.getPlanName()));
    }

    private void row(String label, List<TelecomPlan> plans, Function<TelecomPlan, String> field) {
        StringBuilder sb = new StringBuilder(String.format("%-18s", label));
        plans.forEach(p -> sb.append(String.format("%-18s", field.apply(p))));
        System.out.println(sb);
    }

    private void printPlans(List<TelecomPlan> plans) {
        if (plans.isEmpty()) {
            System.out.println("\nNo plans match.");
            return;
        }
        System.out.println();
        System.out.printf("%-10s %-15s %-9s %12s %8s %10s %10s%n",
                "Code", "Name", "Type", "Rental", "Data", "Voice", "SMS");
        plans.forEach(p -> System.out.printf("%-10s %-15s %-9s %12s %8s %10s %10s%n",
                p.getPlanCode(),
                p.getPlanName(),
                p.getPlanType(),
                String.format("Rs.%.2f", p.getMonthlyRental()),
                String.format("%.0f GB", p.getDataAllowanceGb()),
                limit(p.getVoiceMinutes(), " min"),
                limit(p.getSmsAllowance(), "")));
    }

    private String limit(int value, String unit) {
        return value < 0 ? "Unlimited" : value + unit;
    }
}