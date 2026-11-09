package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.SubscriptionHistory;

import java.sql.Connection;
import java.util.List;

public interface SubscriptionHistoryDAO {
    void insert(Connection con, SubscriptionHistory h);
    List<SubscriptionHistory> findBySubscription(int subscriptionId);
}