package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.AuditDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class AuditDAOImpl implements AuditDAO {

    @Override
    public void insert(Connection con, AuditLog log) {
        String sql = "INSERT INTO audit_log (actor_type, actor_id, action, entity_type, entity_id, details) "
                + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, log.getActorType().name());
            ps.setObject(2, log.getActorId(), Types.INTEGER);
            ps.setString(3, log.getAction());
            ps.setString(4, log.getEntityType());
            ps.setObject(5, log.getEntityId(), Types.INTEGER);
            ps.setString(6, log.getDetails());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not write the audit record", e);
        }
    }
}