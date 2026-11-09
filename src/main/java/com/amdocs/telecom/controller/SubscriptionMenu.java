package com.amdocs.telecom.controller;

import com.amdocs.telecom.dto.LoginSession;
import com.amdocs.telecom.exception.TcsmsException;
import com.amdocs.telecom.exception.ValidationException;
import com.amdocs.telecom.model.AddOn;
import com.amdocs.telecom.model.MobileSubscription;
import com.amdocs.telecom.model.SimType;
import com.amdocs.telecom.model.SubscriptionHistory;
import com.amdocs.telecom.service.PlanService;
import com.amdocs.telecom.service.SubscriptionService;
import com.amdocs.telecom.util.InputHelper;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SubscriptionMenu {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private final SubscriptionService subscriptionService;
    private final PlanService planService;

    public SubscriptionMenu(SubscriptionService subscriptionService, PlanService planService) {
        this.subscriptionService = subscriptionService;
        this.planService = planService;
    }

    public void show(LoginSession session) {
        int customerId = session.getUserId();
        boolean active = true;
        while (active) {
            System.out.println("\n--- My Subscriptions ---");
            System.out.println("1. View my subscriptions");
            System.out.println("2. Subscribe to a plan");
            System.out.println("3. Upgrade or downgrade a plan");
            System.out.println("4. Plan change history");
            System.out.println("5. Manage add-ons");
            System.out.println("6. Back");
            int choice = InputHelper.readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1:
                        printSubscriptions(subscriptionService.getSubscriptions(customerId));
                        break;
                    case 2:
                        subscribe(session);
                        break;
                    case 3:
                        changePlan(session);
                        break;
                    case 4:
                        showHistory(customerId);
                        break;
                    case 5:
                        manageAddOns(customerId);
                        break;
                    case 6:
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

    private void subscribe(LoginSession session) {
        printPlanList();
        String code = InputHelper.readLine("Plan code to subscribe to: ");
        System.out.println("SIM type:  1. Physical SIM   2. eSIM");
        int type = InputHelper.readInt("Enter choice: ");
        if (type != 1 && type != 2) {
            throw new ValidationException("Please choose 1 or 2.");
        }
        MobileSubscription s = subscriptionService.subscribe(session.getUserId(), code,
                type == 2 ? SimType.ESIM : SimType.PHYSICAL_SIM, false, session.getUsername());
        System.out.println("\nSubscribed! Your new number is " + s.getMobileNumber());
        System.out.println("Subscription " + s.getSubscriptionNumber() + " | " + s.getPlanName()
                + " | " + s.getSubscriptionType() + " | " + s.getSimType());
    }

    private void changePlan(LoginSession session) {
        MobileSubscription sub = pick(session.getUserId());
        if (sub == null) {
            return;
        }
        System.out.println("\nCurrent plan: " + sub.getPlanName());
        printPlanList();
        String code = InputHelper.readLine("New plan code: ");
        String kind = subscriptionService.changePlan(session.getUserId(), sub.getSubscriptionId(),
                code, false, session.getUsername());
        System.out.println("\nDone: " + kind + " completed and saved to your plan history.");
    }

    private void showHistory(int customerId) {
        MobileSubscription sub = pick(customerId);
        if (sub == null) {
            return;
        }
        List<SubscriptionHistory> list = subscriptionService.getHistory(customerId, sub.getSubscriptionId());
        System.out.println("\nPlan history for " + sub.getMobileNumber() + ":");
        list.forEach(h -> System.out.printf("  %-18s %-14s -> %-14s %s (by %s)%n",
                FMT.format(h.getChangeDate()),
                h.getOldPlanName() == null ? "(new)" : h.getOldPlanName(),
                h.getNewPlanName(),
                h.getChangeReason(),
                h.getChangedBy()));
    }

    private void manageAddOns(int customerId) {
        MobileSubscription sub = pick(customerId);
        if (sub == null) {
            return;
        }
        boolean active = true;
        while (active) {
            System.out.println("\n--- Add-ons for " + sub.getMobileNumber() + " ---");
            System.out.println("1. View add-ons");
            System.out.println("2. Activate an add-on");
            System.out.println("3. Deactivate an add-on");
            System.out.println("4. Back");
            int choice = InputHelper.readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1:
                        printAddOns(customerId, sub);
                        break;
                    case 2:
                        printAddOns(customerId, sub);
                        subscriptionService.activateAddOn(customerId, sub.getSubscriptionId(),
                                InputHelper.readLine("Add-on code to activate: "));
                        System.out.println("Add-on activated.");
                        break;
                    case 3:
                        printAddOns(customerId, sub);
                        subscriptionService.deactivateAddOn(customerId, sub.getSubscriptionId(),
                                InputHelper.readLine("Add-on code to deactivate: "));
                        System.out.println("Add-on deactivated.");
                        break;
                    case 4:
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

    private void printAddOns(int customerId, MobileSubscription sub) {
        Set<String> activeCodes = subscriptionService.getActiveAddOns(customerId, sub.getSubscriptionId())
                .stream()
                .map(AddOn::getAddOnCode)
                .collect(Collectors.toSet());
        System.out.println();
        subscriptionService.listAddOns().forEach(a -> System.out.printf("%-10s %-28s %10s  %s%n",
                a.getAddOnCode(),
                a.getAddOnName(),
                String.format("Rs.%.2f", a.getMonthlyCharge()),
                activeCodes.contains(a.getAddOnCode()) ? "ACTIVE" : ""));
    }

    private void printPlanList() {
        System.out.println();
        planService.getActivePlans().forEach(p -> System.out.printf("%-10s %-15s %-9s %12s%s%n",
                p.getPlanCode(),
                p.getPlanName(),
                p.getPlanType(),
                String.format("Rs.%.2f", p.getMonthlyRental()),
                p.isRestrictedUpgrade() ? "  (admin only)" : ""));
    }

    private MobileSubscription pick(int customerId) {
        List<MobileSubscription> list = subscriptionService.getSubscriptions(customerId);
        if (list.isEmpty()) {
            System.out.println("\nYou have no subscriptions yet.");
            return null;
        }
        printSubscriptions(list);
        int n = InputHelper.readInt("Select subscription (#): ");
        if (n < 1 || n > list.size()) {
            throw new ValidationException("Invalid selection.");
        }
        return list.get(n - 1);
    }

    private void printSubscriptions(List<MobileSubscription> list) {
        if (list.isEmpty()) {
            System.out.println("\nYou have no subscriptions yet.");
            return;
        }
        System.out.println();
        System.out.printf("%-3s %-10s %-16s %-14s %-9s %-13s %-11s%n",
                "#", "Sub no.", "Mobile number", "Plan", "Type", "SIM", "Status");
        for (int i = 0; i < list.size(); i++) {
            MobileSubscription s = list.get(i);
            System.out.printf("%-3d %-10s %-16s %-14s %-9s %-13s %-11s%n",
                    i + 1, s.getSubscriptionNumber(), s.getMobileNumber(), s.getPlanName(),
                    s.getSubscriptionType(), s.getSimType(), s.getStatus());
        }
    }
}