package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.UsageDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.UsageRecord;
import com.amdocs.telecom.model.UsageType;
import com.amdocs.telecom.model.UsageUnit;
import com.amdocs.telecom.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UsageDAOImpl implements UsageDAO {

    private static final String SELECT_DETAILED =
            "SELECT u.*, ms.customer_id, CONCAT(c.first_name, ' ', c.last_name) AS customer_name "
                    + "FROM usage_record u "
                    + "JOIN mobile_subscription ms ON ms.subscription_id = u.subscription_id "
                    + "JOIN customer c ON c.customer_id = ms.customer_id ";

    @Override
    public void insertBatch(List<UsageRecord> records) {
        if (records.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO usage_record (subscription_id, usage_date, usage_type, quantity, unit, charge) "
                + "VALUES (?,?,?,?,?,?)";
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (UsageRecord r : records) {
                    ps.setInt(1, r.getSubscriptionId());
                    ps.setDate(2, Date.valueOf(r.getUsageDate()));
                    ps.setString(3, r.getUsageType().name());
                    ps.setDouble(4, r.getQuantity());
                    ps.setString(5, r.getUnit().name());
                    ps.setDouble(6, r.getCharge());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            con.commit();
        } catch (SQLException e) {
            rollbackQuietly(con);
            throw new DatabaseException("Could not save usage records", e);
        } finally {
            closeQuietly(con);
        }
    }

    @Override
    public List<UsageRecord> findAllDetailed() {
        return query(SELECT_DETAILED + "ORDER BY u.usage_date, u.usage_id", null);
    }

    @Override
    public List<UsageRecord> findByCustomer(int customerId) {
        return query(SELECT_DETAILED + "WHERE ms.customer_id = ? ORDER BY u.usage_date, u.usage_id", customerId);
    }

    @Override
    public double sumCharges(int subscriptionId, LocalDate from, LocalDate to) {
        String sql = "SELECT COALESCE(SUM(charge), 0) FROM usage_record "
                + "WHERE subscription_id = ? AND usage_date BETWEEN ? AND ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subscriptionId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not total the usage charges", e);
        }
    }

    @Override
    public int countInRange(int subscriptionId, LocalDate from, LocalDate to) {
        String sql = "SELECT COUNT(*) FROM usage_record "
                + "WHERE subscription_id = ? AND usage_date BETWEEN ? AND ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subscriptionId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not count usage records", e);
        }
    }

    private List<UsageRecord> query(String sql, Integer param) {
        List<UsageRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read usage records", e);
        }
        return list;
    }

    private UsageRecord mapRow(ResultSet rs) throws SQLException {
        UsageRecord r = new UsageRecord();
        r.setUsageId(rs.getLong("usage_id"));
        r.setSubscriptionId(rs.getInt("subscription_id"));
        r.setUsageDate(rs.getDate("usage_date").toLocalDate());
        r.setUsageType(UsageType.valueOf(rs.getString("usage_type")));
        r.setQuantity(rs.getDouble("quantity"));
        r.setUnit(UsageUnit.valueOf(rs.getString("unit")));
        r.setCharge(rs.getDouble("charge"));
        r.setProcessed(rs.getBoolean("processed"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setCustomerName(rs.getString("customer_name"));
        return r;
    }

    private void rollbackQuietly(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
                // nothing more we can do
            }
        }
    }

    private void closeQuietly(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignored) {
                // nothing more we can do
            }
        }
    }
}