package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.CustomerDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.AccountStatus;
import com.amdocs.telecom.model.Customer;
import com.amdocs.telecom.util.DBConnection;
import com.amdocs.telecom.util.DbUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

public class CustomerDAOImpl implements CustomerDAO {

    @Override
    public int insert(Customer c) {
        String sql = "INSERT INTO customer (customer_number, first_name, last_name, date_of_birth, "
                + "email, mobile_number, address, city, country, username, password_hash, account_status) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCustomerNumber());
            ps.setString(2, c.getFirstName());
            ps.setString(3, c.getLastName());
            ps.setDate(4, Date.valueOf(c.getDateOfBirth()));
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getMobileNumber());
            ps.setString(7, c.getAddress());
            ps.setString(8, c.getCity());
            ps.setString(9, c.getCountry());
            ps.setString(10, c.getUsername());
            ps.setString(11, c.getPasswordHash());
            ps.setString(12, c.getAccountStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save customer: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        return findOne("username", username);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return findOne("email", email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return exists("username", username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return exists("email", email);
    }

    @Override
    public boolean existsByMobile(String mobileNumber) {
        return exists("mobile_number", mobileNumber);
    }

    @Override
    public void updateLoginState(int customerId, int failedAttempts, LocalDateTime lockedUntil, LocalDateTime lastLogin) {
        String sql = "UPDATE customer SET failed_attempts = ?, locked_until = ?, "
                + "last_login = COALESCE(?, last_login) WHERE customer_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, failedAttempts);
            ps.setTimestamp(2, DbUtil.toTimestamp(lockedUntil));
            ps.setTimestamp(3, DbUtil.toTimestamp(lastLogin));
            ps.setInt(4, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update login state", e);
        }
    }

    @Override
    public void updatePassword(int customerId, String passwordHash) {
        String sql = "UPDATE customer SET password_hash = ?, failed_attempts = 0, locked_until = NULL "
                + "WHERE customer_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update password", e);
        }
    }

    @Override
    public synchronized String nextCustomerNumber() {
        String sql = "SELECT COALESCE(MAX(customer_id), 0) + 1 FROM customer";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return "CUST" + (100000 + rs.getInt(1));
        } catch (SQLException e) {
            throw new DatabaseException("Could not generate customer number", e);
        }
    }

    // column is always one of our own constants, never user input
    private Optional<Customer> findOne(String column, String value) {
        String sql = "SELECT * FROM customer WHERE " + column + " = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read customer", e);
        }
    }

    private boolean exists(String column, String value) {
        String sql = "SELECT 1 FROM customer WHERE " + column + " = ? LIMIT 1";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check customer", e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setCustomerNumber(rs.getString("customer_number"));
        c.setFirstName(rs.getString("first_name"));
        c.setLastName(rs.getString("last_name"));
        c.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        c.setEmail(rs.getString("email"));
        c.setMobileNumber(rs.getString("mobile_number"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        c.setCountry(rs.getString("country"));
        c.setUsername(rs.getString("username"));
        c.setPasswordHash(rs.getString("password_hash"));
        c.setRegistrationDate(rs.getDate("registration_date").toLocalDate());
        c.setAccountStatus(AccountStatus.valueOf(rs.getString("account_status")));
        c.setFailedAttempts(rs.getInt("failed_attempts"));
        c.setLockedUntil(DbUtil.toLocalDateTime(rs.getTimestamp("locked_until")));
        c.setLastLogin(DbUtil.toLocalDateTime(rs.getTimestamp("last_login")));
        c.setCreatedAt(DbUtil.toLocalDateTime(rs.getTimestamp("created_at")));
        c.setUpdatedAt(DbUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
        return c;
    }
}