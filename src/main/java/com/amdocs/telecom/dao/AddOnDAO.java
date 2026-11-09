package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.AddOn;
import com.amdocs.telecom.model.PlanStatus;
import com.amdocs.telecom.model.SubscriptionAddOn;

import java.util.List;
import java.util.Optional;

public interface AddOnDAO {
    List<AddOn> findAllActive();
    Optional<AddOn> findByCode(String code);
    List<AddOn> findActiveForSubscription(int subscriptionId);
    Optional<SubscriptionAddOn> findLink(int subscriptionId, int addOnId);
    void insertLink(int subscriptionId, int addOnId);
    void setLinkStatus(int subscriptionId, int addOnId, PlanStatus status);
}