package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.Administrator;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AdministratorDAO {
    int insert(Administrator admin);
    Optional<Administrator> findByUsername(String username);
    boolean existsByUsername(String username);
    void updateLoginState(int adminId, int failedAttempts, LocalDateTime lockedUntil, LocalDateTime lastLogin);
}