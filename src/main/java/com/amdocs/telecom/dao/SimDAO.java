package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.SIMCard;
import com.amdocs.telecom.model.SimType;

import java.sql.Connection;
import java.util.Optional;

public interface SimDAO {
    Optional<SIMCard> lockFirstAvailable(Connection con, SimType type);
    void markAssigned(Connection con, int simId);
}