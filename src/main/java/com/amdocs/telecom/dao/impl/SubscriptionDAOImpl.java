package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.SubscriptionDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.MobileSubscription;
import com.amdocs.telecom.model.PlanType;
import com.amdocs.telecom.model.SimType;
import com.amdocs.telecom.model.SubscriptionStatus;
import com.amdocs.telecom.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubscriptionDAOImpl implements SubscriptionDAO {

    private static final String SELECT_JOINED =
            "SELECT ms.*, sc.sim_type, tp.plan_name FROM mobile_subscription ms "
                    + "JOIN sim_card sc ON sc.sim_id = ms.sim_id "
                    + "JOIN telecom_plan tp ON tp.plan_id = ms.plan_id ";

    @Override
    public int nextSequence(Connection con) {
        String sql = "SELECT COALESCE(MAX(subscription_id), 0) + 1 FROM mobile_subscription";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DatabaseException("Could not generate a subscription number", e);
        }
    }

    @Override
    public int insert(Connection con, MobileSubscription s) {
        String sql = "INSERT INTO mobile_subscription (subscription_number, customer_id, plan_id, sim_id, "
                + "mobile_number, activation_date, subscription_type, status) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getSubscriptionNumber());
            ps.setInt(2, s.getCustomerId());
            ps.setInt(3, s.getPlanId());
            ps.setInt(4, s.getSimId());
            ps.setString(5, s.getMobileNumber());
            ps.setDate(6, Date.valueOf(s.getActivationDate()));
            ps.setString(7, s.getSubscriptionType().name());
            ps.setString(8, s.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the subscription: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<MobileSubscription> findById(int subscriptionId) {
        String sql = SELECT_JOINED + "WHERE ms.subscription_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read the subscription", e);
        }
    }

    @Override
    public List<MobileSubscription> findByCustomer(int customerId) {
        String sql = SELECT_JOINED + "WHERE ms.customer_id = ? ORDER BY ms.subscription_id";
        List<MobileSubscription> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read subscriptions", e);
        }
        return list;
    }

    @Override
    public boolean hasLiveSubscriptionToPlan(int customerId, int planId) {
        String sql = "SELECT 1 FROM mobile_subscription WHERE customer_id = ? AND plan_id = ? "
                + "AND status <> 'TERMINATED' LIMIT 1";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, planId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check existing subscriptions", e);
        }
    }

    @Override
    public void updatePlan(Connection con, int subscriptionId, int planId, PlanType type) {
        String sql = "UPDATE mobile_subscription SET plan_id = ?, subscription_type = ? WHERE subscription_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ps.setString(2, type.name());
            ps.setInt(3, subscriptionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not change the plan", e);
        }
    }

    private MobileSubscription mapRow(ResultSet rs) throws SQLException {
        MobileSubscription s = new MobileSubscription();
        s.setSubscriptionId(rs.getInt("subscription_id"));
        s.setSubscriptionNumber(rs.getString("subscription_number"));
        s.setCustomerId(rs.getInt("customer_id"));
        s.setPlanId(rs.getInt("plan_id"));
        s.setSimId(rs.getInt("sim_id"));
        s.setMobileNumber(rs.getString("mobile_number"));
        s.setActivationDate(rs.getDate("activation_date").toLocalDate());
        s.setSubscriptionType(PlanType.valueOf(rs.getString("subscription_type")));
        s.setStatus(SubscriptionStatus.valueOf(rs.getString("status")));
        s.setSimType(SimType.valueOf(rs.getString("sim_type")));
        s.setPlanName(rs.getString("plan_name"));
        return s;
    }
}