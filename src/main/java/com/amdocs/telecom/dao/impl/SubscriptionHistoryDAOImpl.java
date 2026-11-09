package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.SubscriptionHistoryDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.SubscriptionHistory;
import com.amdocs.telecom.util.DBConnection;
import com.amdocs.telecom.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionHistoryDAOImpl implements SubscriptionHistoryDAO {

    @Override
    public void insert(Connection con, SubscriptionHistory h) {
        String sql = "INSERT INTO subscription_history (subscription_id, old_plan_id, new_plan_id, "
                + "change_reason, changed_by) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, h.getSubscriptionId());
            ps.setObject(2, h.getOldPlanId(), Types.INTEGER);
            ps.setInt(3, h.getNewPlanId());
            ps.setString(4, h.getChangeReason());
            ps.setString(5, h.getChangedBy());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not record the plan change", e);
        }
    }

    @Override
    public List<SubscriptionHistory> findBySubscription(int subscriptionId) {
        String sql = "SELECT h.*, op.plan_name AS old_plan_name, np.plan_name AS new_plan_name "
                + "FROM subscription_history h "
                + "LEFT JOIN telecom_plan op ON op.plan_id = h.old_plan_id "
                + "JOIN telecom_plan np ON np.plan_id = h.new_plan_id "
                + "WHERE h.subscription_id = ? ORDER BY h.change_date DESC, h.history_id DESC";
        List<SubscriptionHistory> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SubscriptionHistory h = new SubscriptionHistory();
                    h.setHistoryId(rs.getLong("history_id"));
                    h.setSubscriptionId(rs.getInt("subscription_id"));
                    int oldId = rs.getInt("old_plan_id");
                    h.setOldPlanId(rs.wasNull() ? null : oldId);
                    h.setNewPlanId(rs.getInt("new_plan_id"));
                    h.setChangeDate(DbUtil.toLocalDateTime(rs.getTimestamp("change_date")));
                    h.setChangeReason(rs.getString("change_reason"));
                    h.setChangedBy(rs.getString("changed_by"));
                    h.setOldPlanName(rs.getString("old_plan_name"));
                    h.setNewPlanName(rs.getString("new_plan_name"));
                    list.add(h);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read the plan history", e);
        }
        return list;
    }
}