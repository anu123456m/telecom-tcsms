package com.amdocs.telecom.service.impl;

import com.amdocs.telecom.dao.BillDAO;
import com.amdocs.telecom.dao.PlanDAO;
import com.amdocs.telecom.dao.SubscriptionDAO;
import com.amdocs.telecom.dao.UsageDAO;
import com.amdocs.telecom.exception.DuplicateResourceException;
import com.amdocs.telecom.exception.ResourceNotFoundException;
import com.amdocs.telecom.exception.ValidationException;
import com.amdocs.telecom.model.Bill;
import com.amdocs.telecom.model.BillStatus;
import com.amdocs.telecom.model.MobileSubscription;
import com.amdocs.telecom.model.TelecomPlan;
import com.amdocs.telecom.service.BillingService;
import com.amdocs.telecom.service.DiscountStrategy;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class BillingServiceImpl implements BillingService {

    private static final double TAX_RATE = 0.18;

    private final BillDAO billDao;
    private final UsageDAO usageDao;
    private final SubscriptionDAO subscriptionDao;
    private final PlanDAO planDao;
    private final DiscountStrategy discountStrategy;

    public BillingServiceImpl(BillDAO billDao, UsageDAO usageDao, SubscriptionDAO subscriptionDao,
                              PlanDAO planDao, DiscountStrategy discountStrategy) {
        this.billDao = billDao;
        this.usageDao = usageDao;
        this.subscriptionDao = subscriptionDao;
        this.planDao = planDao;
        this.discountStrategy = discountStrategy;
    }

    @Override
    public Bill generateBill(int subscriptionId, YearMonth month) {
        MobileSubscription sub = subscriptionDao.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found."));
        if (month.isBefore(YearMonth.from(sub.getActivationDate()))) {
            throw new ValidationException(sub.getMobileNumber() + " had not started in " + month + ".");
        }
        LocalDate billingMonth = month.atDay(1);
        if (billDao.exists(subscriptionId, billingMonth)) {
            throw new DuplicateResourceException("A bill for " + month + " already exists for "
                    + sub.getMobileNumber() + ".");
        }
        TelecomPlan plan = planDao.findById(sub.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found."));

        double rental = round2(plan.getMonthlyRental());
        double usageCharges = round2(usageDao.sumCharges(subscriptionId, billingMonth, month.atEndOfMonth()));
        double subtotal = rental + usageCharges;
        double tax = round2(subtotal * TAX_RATE);
        double discount = round2(Math.min(discountStrategy.discountFor(plan, subtotal), subtotal));
        double total = round2(rental + usageCharges + tax - discount);

        Bill b = new Bill();
        b.setBillNumber(String.format("INV-%s-%d", month, 10000 + subscriptionId));
        b.setSubscriptionId(subscriptionId);
        b.setBillingMonth(billingMonth);
        b.setPlanRental(rental);
        b.setUsageCharges(usageCharges);
        b.setTaxAmount(tax);
        b.setDiscount(discount);
        b.setTotalAmount(total);
        b.setDueDate(billingMonth.plusMonths(1).plusDays(19));   // 20th of the next month
        b.setBillStatus(BillStatus.GENERATED);
        b.setBillId(billDao.insert(b));
        b.setMobileNumber(sub.getMobileNumber());
        return b;
    }

    @Override
    public int[] generateBillsForMonth(YearMonth month) {
        int generated = 0;
        int skipped = 0;
        for (MobileSubscription sub : subscriptionDao.findAllActive()) {
            try {
                generateBill(sub.getSubscriptionId(), month);
                generated++;
            } catch (DuplicateResourceException | ValidationException e) {
                skipped++;
            }
        }
        return new int[]{generated, skipped};
    }

    @Override
    public List<Bill> getBillsForCustomer(int customerId) {
        return billDao.findByCustomer(customerId);
    }

    @Override
    public List<Bill> getBillsForMonth(YearMonth month) {
        return billDao.findAllDetailed().stream()
                .filter(b -> YearMonth.from(b.getBillingMonth()).equals(month))
                .collect(Collectors.toList());
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}