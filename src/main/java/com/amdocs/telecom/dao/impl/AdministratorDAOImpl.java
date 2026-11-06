package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.AdministratorDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.AccountStatus;
import com.amdocs.telecom.model.AdminRole;
import com.amdocs.telecom.model.Administrator;
import com.amdocs.telecom.util.DBConnection;
import com.amdocs.telecom.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

public class AdministratorDAOImpl implements AdministratorDAO {

    @Override
    public int insert(Administrator a) {
        String sql = "INSERT INTO administrator (username, password_hash, full_name, email, role, status) "
                + "VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getUsername());
            ps.setString(2, a.getPasswordHash());
            ps.setString(3, a.getFullName());
            ps.setString(4, a.getEmail());
            ps.setString(5, a.getRole().name());
            ps.setString(6, a.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save administrator: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Administrator> findByUsername(String username) {
        String sql = "SELECT * FROM administrator WHERE username = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read administrator", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public void updateLoginState(int adminId, int failedAttempts, LocalDateTime lockedUntil, LocalDateTime lastLogin) {
        String sql = "UPDATE administrator SET failed_attempts = ?, locked_until = ?, "
                + "last_login = COALESCE(?, last_login) WHERE admin_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, failedAttempts);
            ps.setTimestamp(2, DbUtil.toTimestamp(lockedUntil));
            ps.setTimestamp(3, DbUtil.toTimestamp(lastLogin));
            ps.setInt(4, adminId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update login state", e);
        }
    }

    private Administrator mapRow(ResultSet rs) throws SQLException {
        Administrator a = new Administrator();
        a.setAdminId(rs.getInt("admin_id"));
        a.setUsername(rs.getString("username"));
        a.setPasswordHash(rs.getString("password_hash"));
        a.setFullName(rs.getString("full_name"));
        a.setEmail(rs.getString("email"));
        a.setRole(AdminRole.valueOf(rs.getString("role")));
        a.setStatus(AccountStatus.valueOf(rs.getString("status")));
        a.setFailedAttempts(rs.getInt("failed_attempts"));
        a.setLockedUntil(DbUtil.toLocalDateTime(rs.getTimestamp("locked_until")));
        a.setLastLogin(DbUtil.toLocalDateTime(rs.getTimestamp("last_login")));
        a.setCreatedAt(DbUtil.toLocalDateTime(rs.getTimestamp("created_at")));
        a.setUpdatedAt(DbUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
        return a;
    }
}