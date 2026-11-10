package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.AuditLog;

import java.sql.Connection;

public interface AuditDAO {
    void insert(Connection con, AuditLog log);
}