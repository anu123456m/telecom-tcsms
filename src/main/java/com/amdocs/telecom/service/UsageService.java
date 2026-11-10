package com.amdocs.telecom.service;

import com.amdocs.telecom.model.UsageType;

import java.time.YearMonth;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

public interface UsageService {
    int generateSampleUsage(YearMonth month);
    Map<UsageType, Double> totalsByType();
    Map<YearMonth, Map<UsageType, Double>> monthlyUsage();
    List<Map.Entry<String, Double>> topDataConsumers(int limit);
    DoubleSummaryStatistics dataSessionStats();
    Map<UsageType, Double> totalsForCustomer(int customerId);
}