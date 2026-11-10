package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.PaymentDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.Payment;
import com.amdocs.telecom.model.PaymentMode;
import com.amdocs.telecom.model.PaymentStatus;
import com.amdocs.telecom.util.DBConnection;
import com.amdocs.telecom.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAOImpl implements PaymentDAO {

    private static final String SELECT_DETAILED =
            "SELECT p.*, b.bill_number, CONCAT(c.first_name, ' ', c.last_name) AS customer_name "
                    + "FROM payment p "
                    + "JOIN bill b ON b.bill_id = p.bill_id "
                    + "JOIN customer c ON c.customer_id = p.customer_id ";

    @Override
    public int insert(Connection con, Payment p) {
        String sql = "INSERT INTO payment (transaction_reference, bill_id, customer_id, amount, "
                + "payment_mode, payment_status) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTransactionReference());
            ps.setInt(2, p.getBillId());
            ps.setInt(3, p.getCustomerId());
            ps.setDouble(4, p.getAmount());
            ps.setString(5, p.getPaymentMode().name());
            ps.setString(6, p.getPaymentStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the payment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasSuccessfulPayment(Connection con, int billId) {
        String sql = "SELECT 1 FROM payment WHERE bill_id = ? AND payment_status = 'SUCCESS' LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check earlier payments", e);
        }
    }

    @Override
    public List<Payment> findByCustomer(int customerId) {
        return query(SELECT_DETAILED + "WHERE p.customer_id = ? ORDER BY p.payment_date DESC, p.payment_id DESC",
                customerId);
    }

    @Override
    public List<Payment> findAllDetailed() {
        return query(SELECT_DETAILED + "ORDER BY p.payment_date DESC, p.payment_id DESC", null);
    }

    private List<Payment> query(String sql, Integer param) {
        List<Payment> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setPaymentId(rs.getInt("payment_id"));
                    p.setTransactionReference(rs.getString("transaction_reference"));
                    p.setBillId(rs.getInt("bill_id"));
                    p.setCustomerId(rs.getInt("customer_id"));
                    p.setAmount(rs.getDouble("amount"));
                    p.setPaymentMode(PaymentMode.valueOf(rs.getString("payment_mode")));
                    p.setPaymentDate(DbUtil.toLocalDateTime(rs.getTimestamp("payment_date")));
                    p.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
                    p.setBillNumber(rs.getString("bill_number"));
                    p.setCustomerName(rs.getString("customer_name"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read payments", e);
        }
        return list;
    }
}