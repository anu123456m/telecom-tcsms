package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.OtpDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.PasswordResetOtp;
import com.amdocs.telecom.util.DBConnection;
import com.amdocs.telecom.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class OtpDAOImpl implements OtpDAO {

    @Override
    public int insert(PasswordResetOtp otp) {
        String sql = "INSERT INTO password_reset_otp (customer_id, otp_code, expires_at) VALUES (?,?,?)";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, otp.getCustomerId());
            ps.setString(2, otp.getOtpCode());
            ps.setTimestamp(3, DbUtil.toTimestamp(otp.getExpiresAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save OTP", e);
        }
    }

    @Override
    public Optional<PasswordResetOtp> findLatestUnused(int customerId) {
        String sql = "SELECT * FROM password_reset_otp WHERE customer_id = ? AND is_used = FALSE "
                + "ORDER BY otp_id DESC LIMIT 1";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                PasswordResetOtp o = new PasswordResetOtp();
                o.setOtpId(rs.getInt("otp_id"));
                o.setCustomerId(rs.getInt("customer_id"));
                o.setOtpCode(rs.getString("otp_code"));
                o.setExpiresAt(DbUtil.toLocalDateTime(rs.getTimestamp("expires_at")));
                o.setUsed(rs.getBoolean("is_used"));
                return Optional.of(o);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read OTP", e);
        }
    }

    @Override
    public void markUsed(int otpId) {
        executeUpdate("UPDATE password_reset_otp SET is_used = TRUE WHERE otp_id = ?", otpId);
    }

    @Override
    public void invalidateAll(int customerId) {
        executeUpdate("UPDATE password_reset_otp SET is_used = TRUE WHERE customer_id = ? AND is_used = FALSE",
                customerId);
    }

    private void executeUpdate(String sql, int param) {
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, param);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update OTP", e);
        }
    }
}