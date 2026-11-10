package com.amdocs.telecom.controller;

import com.amdocs.telecom.dto.LoginSession;
import com.amdocs.telecom.exception.TcsmsException;
import com.amdocs.telecom.exception.ValidationException;
import com.amdocs.telecom.model.Bill;
import com.amdocs.telecom.model.Payment;
import com.amdocs.telecom.model.PaymentMode;
import com.amdocs.telecom.service.PaymentService;
import com.amdocs.telecom.util.InputHelper;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentMenu {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private final PaymentService paymentService;

    public PaymentMenu(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void showCustomer(LoginSession session) {
        boolean active = true;
        while (active) {
            System.out.println("\n--- Payments ---");
            System.out.println("1. View unpaid bills");
            System.out.println("2. Pay a bill");
            System.out.println("3. Payment history");
            System.out.println("4. Back");
            int choice = InputHelper.readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1:
                        printBills(paymentService.getPayableBills(session.getUserId()));
                        break;
                    case 2:
                        pay(session);
                        break;
                    case 3:
                        printPayments(paymentService.getPaymentsForCustomer(session.getUserId()), false);
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

    public void showAdmin() {
        printPayments(paymentService.getAllPayments(), true);
    }

    private void pay(LoginSession session) {
        List<Bill> bills = paymentService.getPayableBills(session.getUserId());
        if (bills.isEmpty()) {
            System.out.println("\nYou have no unpaid bills.");
            return;
        }
        printBills(bills);
        int n = InputHelper.readInt("Select bill (#): ");
        if (n < 1 || n > bills.size()) {
            throw new ValidationException("Invalid selection.");
        }
        Bill bill = bills.get(n - 1);

        System.out.println("Payment mode:  1. UPI   2. CARD   3. NET_BANKING   4. BANK_TRANSFER");
        int m = InputHelper.readInt("Enter choice: ");
        PaymentMode[] modes = PaymentMode.values();
        if (m < 1 || m > modes.length) {
            throw new ValidationException("Invalid payment mode.");
        }
        double amount = InputHelper.readDouble(
                String.format("Amount to pay (bill total is Rs.%.2f): ", bill.getTotalAmount()));

        Payment p = paymentService.pay(session.getUserId(), bill.getBillId(), amount, modes[m - 1]);
        System.out.println("\nPayment successful!");
        System.out.println("  Reference : " + p.getTransactionReference());
        System.out.println("  Bill      : " + p.getBillNumber());
        System.out.printf("  Amount    : Rs.%.2f%n", p.getAmount());
        System.out.println("  Mode      : " + p.getPaymentMode());
    }

    private void printBills(List<Bill> bills) {
        if (bills.isEmpty()) {
            System.out.println("\nNo unpaid bills.");
            return;
        }
        System.out.println();
        System.out.printf("%-3s %-18s %-8s %10s %-12s %-10s%n", "#", "Bill no.", "Month", "Total", "Due", "Status");
        for (int i = 0; i < bills.size(); i++) {
            Bill b = bills.get(i);
            System.out.printf("%-3d %-18s %-8s %10.2f %-12s %-10s%n", i + 1, b.getBillNumber(),
                    YearMonth.from(b.getBillingMonth()), b.getTotalAmount(), b.getDueDate(), b.getBillStatus());
        }
    }

    private void printPayments(List<Payment> payments, boolean showCustomer) {
        if (payments.isEmpty()) {
            System.out.println("\nNo payments found.");
            return;
        }
        System.out.println();
        System.out.printf("%-17s %-18s %-20s %10s %-14s %-17s %-8s%n",
                "Reference", "Bill no.", showCustomer ? "Customer" : "", "Amount", "Mode", "Date", "Status");
        payments.forEach(p -> System.out.printf("%-17s %-18s %-20s %10.2f %-14s %-17s %-8s%n",
                p.getTransactionReference(),
                p.getBillNumber(),
                showCustomer ? p.getCustomerName() : "",
                p.getAmount(),
                p.getPaymentMode(),
                FMT.format(p.getPaymentDate()),
                p.getPaymentStatus()));
    }
}