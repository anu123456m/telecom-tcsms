package com.amdocs.telecom.dao.impl;

import com.amdocs.telecom.dao.PlanDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.model.PlanStatus;
import com.amdocs.telecom.model.PlanType;
import com.amdocs.telecom.model.TelecomPlan;
import com.amdocs.telecom.util.DBConnection;
import com.amdocs.telecom.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlanDAOImpl implements PlanDAO {

    @Override
    public List<TelecomPlan> findAll() {
        String sql = "SELECT * FROM telecom_plan ORDER BY plan_id";
        List<TelecomPlan> plans = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                plans.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read plans", e);
        }
        return plans;
    }

    @Override
    public Optional<TelecomPlan> findByCode(String planCode) {
        return findOne("SELECT * FROM telecom_plan WHERE plan_code = ?", planCode);
    }

    @Override
    public Optional<TelecomPlan> findById(int planId) {
        return findOne("SELECT * FROM telecom_plan WHERE plan_id = ?", String.valueOf(planId));
    }

    private Optional<TelecomPlan> findOne(String sql, String value) {
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read plan", e);
        }
    }

    private TelecomPlan mapRow(ResultSet rs) throws SQLException {
        TelecomPlan p = new TelecomPlan();
        p.setPlanId(rs.getInt("plan_id"));
        p.setPlanCode(rs.getString("plan_code"));
        p.setPlanName(rs.getString("plan_name"));
        p.setPlanType(PlanType.valueOf(rs.getString("plan_type")));
        p.setMonthlyRental(rs.getDouble("monthly_rental"));
        p.setDataAllowanceGb(rs.getDouble("data_allowance_gb"));
        p.setVoiceMinutes(rs.getInt("voice_minutes"));
        p.setSmsAllowance(rs.getInt("sms_allowance"));
        p.setValidityDays(rs.getInt("validity_days"));
        p.setInternationalRoaming(rs.getBoolean("international_roaming"));
        p.setRestrictedUpgrade(rs.getBoolean("restricted_upgrade"));
        p.setStatus(PlanStatus.valueOf(rs.getString("status")));
        p.setCreatedAt(DbUtil.toLocalDateTime(rs.getTimestamp("created_at")));
        p.setUpdatedAt(DbUtil.toLocalDateTime(rs.getTimestamp("updated_at")));
        return p;
    }
}