package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.AddOnDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.AddOn;
import com.amdocs.telecom.model.PlanStatus;
import com.amdocs.telecom.model.SubscriptionAddOn;
import com.amdocs.telecom.util.DBConnection;
import com.amdocs.telecom.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddOnDAOImpl implements AddOnDAO {

    @Override
    public List<AddOn> findAllActive() {
        return query("SELECT * FROM add_on WHERE status = 'ACTIVE' ORDER BY add_on_id", null);
    }

    @Override
    public Optional<AddOn> findByCode(String code) {
        List<AddOn> list = query("SELECT * FROM add_on WHERE add_on_code = ?", code);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<AddOn> findActiveForSubscription(int subscriptionId) {
        return query("SELECT a.* FROM add_on a JOIN subscription_add_on sa ON sa.add_on_id = a.add_on_id "
                        + "WHERE sa.status = 'ACTIVE' AND sa.subscription_id = ? ORDER BY a.add_on_id",
                String.valueOf(subscriptionId));
    }

    @Override
    public Optional<SubscriptionAddOn> findLink(int subscriptionId, int addOnId) {
        String sql = "SELECT * FROM subscription_add_on WHERE subscription_id = ? AND add_on_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subscriptionId);
            ps.setInt(2, addOnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                SubscriptionAddOn l = new SubscriptionAddOn();
                l.setSubAddonId(rs.getInt("sub_addon_id"));
                l.setSubscriptionId(rs.getInt("subscription_id"));
                l.setAddOnId(rs.getInt("add_on_id"));
                l.setActivatedOn(DbUtil.toLocalDateTime(rs.getTimestamp("activated_on")));
                l.setDeactivatedOn(DbUtil.toLocalDateTime(rs.getTimestamp("deactivated_on")));
                l.setStatus(PlanStatus.valueOf(rs.getString("status")));
                return Optional.of(l);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read the add-on", e);
        }
    }

    @Override
    public void insertLink(int subscriptionId, int addOnId) {
        execute("INSERT INTO subscription_add_on (subscription_id, add_on_id) VALUES (?, ?)",
                subscriptionId, addOnId);
    }

    @Override
    public void setLinkStatus(int subscriptionId, int addOnId, PlanStatus status) {
        String sql = status == PlanStatus.ACTIVE
                ? "UPDATE subscription_add_on SET status = 'ACTIVE', activated_on = NOW(), deactivated_on = NULL "
                + "WHERE subscription_id = ? AND add_on_id = ?"
                : "UPDATE subscription_add_on SET status = 'INACTIVE', deactivated_on = NOW() "
                + "WHERE subscription_id = ? AND add_on_id = ?";
        execute(sql, subscriptionId, addOnId);
    }

    private void execute(String sql, int first, int second) {
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, first);
            ps.setInt(2, second);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the add-on", e);
        }
    }

    private List<AddOn> query(String sql, String param) {
        List<AddOn> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AddOn a = new AddOn();
                    a.setAddOnId(rs.getInt("add_on_id"));
                    a.setAddOnCode(rs.getString("add_on_code"));
                    a.setAddOnName(rs.getString("add_on_name"));
                    a.setDescription(rs.getString("description"));
                    a.setMonthlyCharge(rs.getDouble("monthly_charge"));
                    a.setStatus(PlanStatus.valueOf(rs.getString("status")));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read add-ons", e);
        }
        return list;
    }
}