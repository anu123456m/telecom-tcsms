package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.NotificationDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NotificationDAOImpl implements NotificationDAO {

    @Override
    public void insert(Connection con, Notification n) {
        String sql = "INSERT INTO notification (customer_id, notification_type, message, status) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, n.getCustomerId());
            ps.setString(2, n.getNotificationType().name());
            ps.setString(3, n.getMessage());
            ps.setString(4, n.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not queue the notification", e);
        }
    }
}