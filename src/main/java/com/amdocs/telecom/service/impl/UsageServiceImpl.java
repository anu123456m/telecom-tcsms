package com.amdocs.telecom.service.impl;

import com.amdocs.telecom.dao.PlanDAO;
import com.amdocs.telecom.dao.SubscriptionDAO;
import com.amdocs.telecom.dao.UsageDAO;
import com.amdocs.telecom.model.MobileSubscription;
import com.amdocs.telecom.model.TelecomPlan;
import com.amdocs.telecom.model.UsageRecord;
import com.amdocs.telecom.model.UsageType;
import com.amdocs.telecom.model.UsageUnit;
import com.amdocs.telecom.service.UsageService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class UsageServiceImpl implements UsageService {

    private static final int RECORDS_PER_SUBSCRIPTION = 8;
    private static final double ROAMING_RATE_PER_MB = 0.50;

    private final UsageDAO usageDao;
    private final SubscriptionDAO subscriptionDao;
    private final PlanDAO planDao;
    private final Random random = new Random();

    public UsageServiceImpl(UsageDAO usageDao, SubscriptionDAO subscriptionDao, PlanDAO planDao) {
        this.usageDao = usageDao;
        this.subscriptionDao = subscriptionDao;
        this.planDao = planDao;
    }

    @Override
    public int generateSampleUsage(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        List<UsageRecord> toSave = new ArrayList<>();

        for (MobileSubscription sub : subscriptionDao.findAllActive()) {
            if (YearMonth.from(sub.getActivationDate()).isAfter(month)) {
                continue;   // subscription had not started yet
            }
            if (usageDao.countInRange(sub.getSubscriptionId(), from, to) > 0) {
                continue;   // already has usage for this month
            }
            boolean roaming = planDao.findById(sub.getPlanId())
                    .map(TelecomPlan::isInternationalRoaming)
                    .orElse(false);
            for (int i = 0; i < RECORDS_PER_SUBSCRIPTION; i++) {
                toSave.add(randomRecord(sub.getSubscriptionId(), month, roaming));
            }
        }
        usageDao.insertBatch(toSave);
        return toSave.size();
    }

    @Override
    public Map<UsageType, Double> totalsByType() {
        return usageDao.findAllDetailed().stream()
                .collect(Collectors.groupingBy(UsageRecord::getUsageType,
                        Collectors.summingDouble(UsageRecord::getQuantity)));
    }

    @Override
    public Map<YearMonth, Map<UsageType, Double>> monthlyUsage() {
        return usageDao.findAllDetailed().stream()
                .collect(Collectors.groupingBy(r -> YearMonth.from(r.getUsageDate()), TreeMap::new,
                        Collectors.groupingBy(UsageRecord::getUsageType,
                                Collectors.summingDouble(UsageRecord::getQuantity))));
    }

    @Override
    public List<Map.Entry<String, Double>> topDataConsumers(int limit) {
        Map<String, Double> byCustomer = usageDao.findAllDetailed().stream()
                .filter(r -> r.getUsageType() == UsageType.DATA)
                .collect(Collectors.groupingBy(UsageRecord::getCustomerName,
                        Collectors.summingDouble(UsageRecord::getQuantity)));
        return byCustomer.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public DoubleSummaryStatistics dataSessionStats() {
        return usageDao.findAllDetailed().stream()
                .filter(r -> r.getUsageType() == UsageType.DATA)
                .collect(Collectors.summarizingDouble(UsageRecord::getQuantity));
    }

    @Override
    public Map<UsageType, Double> totalsForCustomer(int customerId) {
        return usageDao.findByCustomer(customerId).stream()
                .collect(Collectors.groupingBy(UsageRecord::getUsageType,
                        Collectors.summingDouble(UsageRecord::getQuantity)));
    }

    private UsageRecord randomRecord(int subscriptionId, YearMonth month, boolean roaming) {
        UsageRecord r = new UsageRecord();
        r.setSubscriptionId(subscriptionId);
        r.setUsageDate(month.atDay(1 + random.nextInt(month.lengthOfMonth())));

        switch (random.nextInt(roaming ? 4 : 3)) {
            case 0:
                r.setUsageType(UsageType.DATA);
                r.setQuantity(Math.round((0.5 + random.nextDouble() * 3.0) * 10) / 10.0);
                r.setUnit(UsageUnit.GB);
                break;
            case 1:
                r.setUsageType(UsageType.VOICE);
                r.setQuantity(10 + random.nextInt(111));
                r.setUnit(UsageUnit.MINUTES);
                break;
            case 2:
                r.setUsageType(UsageType.SMS);
                r.setQuantity(5 + random.nextInt(56));
                r.setUnit(UsageUnit.COUNT);
                break;
            default:
                double mb = 50 + random.nextInt(351);
                r.setUsageType(UsageType.ROAMING);
                r.setQuantity(mb);
                r.setUnit(UsageUnit.MB);
                r.setCharge(mb * ROAMING_RATE_PER_MB);
        }
        return r;
    }
}