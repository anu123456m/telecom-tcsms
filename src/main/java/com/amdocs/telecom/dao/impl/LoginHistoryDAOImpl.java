package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.LoginHistoryDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.LoginHistory;
import com.amdocs.telecom.model.LoginStatus;
import com.amdocs.telecom.model.UserType;
import com.amdocs.telecom.util.DBConnection;
import com.amdocs.telecom.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class LoginHistoryDAOImpl implements LoginHistoryDAO {

    @Override
    public long insert(LoginHistory h) {
        String sql = "INSERT INTO login_history (user_type, customer_id, admin_id, username_attempted, "
                + "login_status, failure_reason) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, h.getUserType().name());
            ps.setObject(2, h.getCustomerId(), Types.INTEGER);
            ps.setObject(3, h.getAdminId(), Types.INTEGER);
            ps.setString(4, h.getUsernameAttempted());
            ps.setString(5, h.getLoginStatus().name());
            ps.setString(6, h.getFailureReason());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not record login", e);
        }
    }

    @Override
    public void recordLogout(long loginId) {
        String sql = "UPDATE login_history SET logout_time = NOW() WHERE login_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, loginId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not record logout", e);
        }
    }

    @Override
    public List<LoginHistory> findRecent(UserType userType, int userId, int limit) {
        String column = (userType == UserType.CUSTOMER) ? "customer_id" : "admin_id";
        String sql = "SELECT * FROM login_history WHERE " + column + " = ? ORDER BY login_time DESC LIMIT ?";
        List<LoginHistory> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LoginHistory h = new LoginHistory();
                    h.setLoginId(rs.getLong("login_id"));
                    h.setUserType(UserType.valueOf(rs.getString("user_type")));
                    h.setUsernameAttempted(rs.getString("username_attempted"));
                    h.setLoginTime(DbUtil.toLocalDateTime(rs.getTimestamp("login_time")));
                    h.setLogoutTime(DbUtil.toLocalDateTime(rs.getTimestamp("logout_time")));
                    h.setLoginStatus(LoginStatus.valueOf(rs.getString("login_status")));
                    h.setFailureReason(rs.getString("failure_reason"));
                    list.add(h);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read login history", e);
        }
        return list;
    }
}
