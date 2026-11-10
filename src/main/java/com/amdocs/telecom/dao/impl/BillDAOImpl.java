package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.BillDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.Bill;
import com.amdocs.telecom.model.BillStatus;
import com.amdocs.telecom.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillDAOImpl implements BillDAO {

    private static final String SELECT_DETAILED =
            "SELECT b.*, ms.mobile_number, ms.customer_id, "
                    + "CONCAT(c.first_name, ' ', c.last_name) AS customer_name "
                    + "FROM bill b "
                    + "JOIN mobile_subscription ms ON ms.subscription_id = b.subscription_id "
                    + "JOIN customer c ON c.customer_id = ms.customer_id ";

    @Override
    public boolean exists(int subscriptionId, LocalDate billingMonth) {
        String sql = "SELECT 1 FROM bill WHERE subscription_id = ? AND billing_month = ? LIMIT 1";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subscriptionId);
            ps.setDate(2, Date.valueOf(billingMonth));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check existing bills", e);
        }
    }

    @Override
    public int insert(Bill b) {
        String sql = "INSERT INTO bill (bill_number, subscription_id, billing_month, plan_rental, "
                + "usage_charges, tax_amount, discount, total_amount, due_date, bill_status) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getBillNumber());
            ps.setInt(2, b.getSubscriptionId());
            ps.setDate(3, Date.valueOf(b.getBillingMonth()));
            ps.setDouble(4, b.getPlanRental());
            ps.setDouble(5, b.getUsageCharges());
            ps.setDouble(6, b.getTaxAmount());
            ps.setDouble(7, b.getDiscount());
            ps.setDouble(8, b.getTotalAmount());
            ps.setDate(9, Date.valueOf(b.getDueDate()));
            ps.setString(10, b.getBillStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the bill: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Bill> findByCustomer(int customerId) {
        return query(SELECT_DETAILED + "WHERE ms.customer_id = ? ORDER BY b.billing_month DESC, b.bill_id DESC",
                customerId);
    }

    @Override
    public List<Bill> findAllDetailed() {
        return query(SELECT_DETAILED + "ORDER BY b.billing_month DESC, b.bill_id", null);
    }

    /** Locks only the bill row (FOR UPDATE OF b), so a second payment waits until this one finishes. */
    @Override
    public Optional<Bill> findByIdForUpdate(Connection con, int billId) {
        String sql = SELECT_DETAILED + "WHERE b.bill_id = ? FOR UPDATE OF b";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read the bill", e);
        }
    }

    @Override
    public void updateStatus(Connection con, int billId, BillStatus status) {
        String sql = "UPDATE bill SET bill_status = ? WHERE bill_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, billId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the bill status", e);
        }
    }

    private List<Bill> query(String sql, Integer param) {
        List<Bill> list = new ArrayList<>();
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
            throw new DatabaseException("Could not read bills", e);
        }
        return list;
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id"));
        b.setBillNumber(rs.getString("bill_number"));
        b.setSubscriptionId(rs.getInt("subscription_id"));
        b.setBillingMonth(rs.getDate("billing_month").toLocalDate());
        b.setPlanRental(rs.getDouble("plan_rental"));
        b.setUsageCharges(rs.getDouble("usage_charges"));
        b.setTaxAmount(rs.getDouble("tax_amount"));
        b.setDiscount(rs.getDouble("discount"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setDueDate(rs.getDate("due_date").toLocalDate());
        b.setBillStatus(BillStatus.valueOf(rs.getString("bill_status")));
        b.setMobileNumber(rs.getString("mobile_number"));
        b.setCustomerId(rs.getInt("customer_id"));
        b.setCustomerName(rs.getString("customer_name"));
        return b;
    }
}