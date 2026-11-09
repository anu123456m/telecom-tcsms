package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.TelecomPlan;

import java.util.List;
import java.util.Optional;

public interface PlanDAO {
    List<TelecomPlan> findAll();
    Optional<TelecomPlan> findByCode(String planCode);
    Optional<TelecomPlan> findById(int planId);
}