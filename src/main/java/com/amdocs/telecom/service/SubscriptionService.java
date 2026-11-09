package com.amdocs.telecom.service;

import com.amdocs.telecom.model.AddOn;
import com.amdocs.telecom.model.MobileSubscription;
import com.amdocs.telecom.model.SimType;
import com.amdocs.telecom.model.SubscriptionHistory;

import java.util.List;

public interface SubscriptionService {
    MobileSubscription subscribe(int customerId, String planCode, SimType simType, boolean byAdmin, String changedBy);
    List<MobileSubscription> getSubscriptions(int customerId);

    /** Returns UPGRADE, DOWNGRADE or CHANGE. */
    String changePlan(int customerId, int subscriptionId, String newPlanCode, boolean byAdmin, String changedBy);
    List<SubscriptionHistory> getHistory(int customerId, int subscriptionId);

    List<AddOn> listAddOns();
    List<AddOn> getActiveAddOns(int customerId, int subscriptionId);
    void activateAddOn(int customerId, int subscriptionId, String addOnCode);
    void deactivateAddOn(int customerId, int subscriptionId, String addOnCode);
}