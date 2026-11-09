package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.MobileSubscription;
import com.amdocs.telecom.model.PlanType;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface SubscriptionDAO {
    int nextSequence(Connection con);
    int insert(Connection con, MobileSubscription s);
    Optional<MobileSubscription> findById(int subscriptionId);
    List<MobileSubscription> findByCustomer(int customerId);
    boolean hasLiveSubscriptionToPlan(int customerId, int planId);
    void updatePlan(Connection con, int subscriptionId, int planId, PlanType type);
}