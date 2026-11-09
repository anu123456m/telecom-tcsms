package com.amdocs.telecom.service;

import com.amdocs.telecom.model.TelecomPlan;

import java.util.List;

public interface PlanService {
    List<TelecomPlan> getActivePlans();
    List<TelecomPlan> searchByName(String keyword);
    List<TelecomPlan> filterByPriceRange(double min, double max);
    List<TelecomPlan> filterByMinData(double minGb);
    List<TelecomPlan> sortByPrice(boolean ascending);
    List<TelecomPlan> compare(List<String> planCodes);
}