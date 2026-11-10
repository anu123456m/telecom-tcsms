package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.Notification;

import java.sql.Connection;

public interface NotificationDAO {
    void insert(Connection con, Notification n);
}