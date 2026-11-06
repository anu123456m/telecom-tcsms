package com.amdocs.telecom.dao;

import com.amdocs.telecom.model.LoginHistory;
import com.amdocs.telecom.model.UserType;

import java.util.List;

public interface LoginHistoryDAO {
    long insert(LoginHistory history);
    void recordLogout(long loginId);
    List<LoginHistory> findRecent(UserType userType, int userId, int limit);
}