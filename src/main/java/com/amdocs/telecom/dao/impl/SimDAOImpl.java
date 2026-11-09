package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.SimDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.SIMCard;
import com.amdocs.telecom.model.SimStatus;
import com.amdocs.telecom.model.SimType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SimDAOImpl implements SimDAO {

    @Override
    public Optional<SIMCard> lockFirstAvailable(Connection con, SimType type) {
        String sql = "SELECT * FROM sim_card WHERE sim_status = 'AVAILABLE' AND sim_type = ? "
                + "ORDER BY sim_id LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                SIMCard s = new SIMCard();
                s.setSimId(rs.getInt("sim_id"));
                s.setSimNumber(rs.getString("sim_number"));
                s.setSimType(SimType.valueOf(rs.getString("sim_type")));
                s.setSimStatus(SimStatus.valueOf(rs.getString("sim_status")));
                return Optional.of(s);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not look up a free SIM", e);
        }
    }

    @Override
    public void markAssigned(Connection con, int simId) {
        String sql = "UPDATE sim_card SET sim_status = 'ASSIGNED' WHERE sim_id = ? AND sim_status = 'AVAILABLE'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, simId);
            if (ps.executeUpdate() != 1) {
                throw new DatabaseException("The SIM is no longer available", null);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not assign the SIM", e);
        }
    }
}