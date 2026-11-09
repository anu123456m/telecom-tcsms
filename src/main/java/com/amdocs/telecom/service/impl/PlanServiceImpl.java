package com.amdocs.telecom.service.impl;

import com.amdocs.telecom.dao.PlanDAO;
import com.amdocs.telecom.exception.ResourceNotFoundException;
import com.amdocs.telecom.exception.ValidationException;
import com.amdocs.telecom.model.PlanStatus;
import com.amdocs.telecom.model.TelecomPlan;
import com.amdocs.telecom.service.PlanService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PlanServiceImpl implements PlanService {

    private static final Predicate<TelecomPlan> IS_ACTIVE = p -> p.getStatus() == PlanStatus.ACTIVE;
    private static final Comparator<TelecomPlan> BY_PRICE =
            Comparator.comparingDouble(TelecomPlan::getMonthlyRental);

    private final PlanDAO planDao;

    public PlanServiceImpl(PlanDAO planDao) {
        this.planDao = planDao;
    }

    @Override
    public List<TelecomPlan> getActivePlans() {
        return select(IS_ACTIVE, BY_PRICE);
    }

    @Override
    public List<TelecomPlan> searchByName(String keyword) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        Predicate<TelecomPlan> matches = p ->
                p.getPlanName().toLowerCase().contains(key) || p.getPlanCode().toLowerCase().contains(key);
        return select(IS_ACTIVE.and(matches), BY_PRICE);
    }

    @Override
    public List<TelecomPlan> filterByPriceRange(double min, double max) {
        if (min < 0 || max < min) {
            throw new ValidationException("Enter a valid range: minimum must not be above maximum.");
        }
        Predicate<TelecomPlan> inRange = p -> p.getMonthlyRental() >= min && p.getMonthlyRental() <= max;
        return select(IS_ACTIVE.and(inRange), BY_PRICE);
    }

    @Override
    public List<TelecomPlan> filterByMinData(double minGb) {
        if (minGb < 0) {
            throw new ValidationException("Data allowance cannot be negative.");
        }
        Predicate<TelecomPlan> enoughData = p -> p.getDataAllowanceGb() >= minGb;
        return select(IS_ACTIVE.and(enoughData), BY_PRICE);
    }

    @Override
    public List<TelecomPlan> sortByPrice(boolean ascending) {
        return select(IS_ACTIVE, ascending ? BY_PRICE : BY_PRICE.reversed());
    }

    @Override
    public List<TelecomPlan> compare(List<String> planCodes) {
        List<String> codes = planCodes.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(c -> !c.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (codes.size() < 2 || codes.size() > 3) {
            throw new ValidationException("Enter 2 or 3 different plan codes to compare.");
        }
        Map<String, TelecomPlan> byCode = getActivePlans().stream()
                .collect(Collectors.toMap(p -> p.getPlanCode().toUpperCase(), Function.identity()));

        return codes.stream()
                .map(code -> Optional.ofNullable(byCode.get(code))
                        .orElseThrow(() -> new ResourceNotFoundException("No active plan with code " + code)))
                .collect(Collectors.toList());
    }

    private List<TelecomPlan> select(Predicate<TelecomPlan> filter, Comparator<TelecomPlan> order) {
        return planDao.findAll().stream()
                .filter(filter)
                .sorted(order)
                .collect(Collectors.toList());
    }
}