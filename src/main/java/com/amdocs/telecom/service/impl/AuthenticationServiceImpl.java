package com.amdocs.telecom.service.impl;

import com.amdocs.telecom.dao.AdministratorDAO;
import com.amdocs.telecom.dao.CustomerDAO;
import com.amdocs.telecom.dao.LoginHistoryDAO;
import com.amdocs.telecom.dao.OtpDAO;
import com.amdocs.telecom.dto.LoginSession;
import com.amdocs.telecom.exception.AccountLockedException;
import com.amdocs.telecom.exception.AuthenticationFailedException;
import com.amdocs.telecom.exception.ResourceNotFoundException;
import com.amdocs.telecom.exception.ValidationException;
import com.amdocs.telecom.model.AccountStatus;
import com.amdocs.telecom.model.Administrator;
import com.amdocs.telecom.model.Customer;
import com.amdocs.telecom.model.LoginHistory;
import com.amdocs.telecom.model.LoginStatus;
import com.amdocs.telecom.model.PasswordResetOtp;
import com.amdocs.telecom.model.User;
import com.amdocs.telecom.model.UserType;
import com.amdocs.telecom.security.OTPService;
import com.amdocs.telecom.security.PasswordUtil;
import com.amdocs.telecom.service.AuthenticationService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class AuthenticationServiceImpl implements AuthenticationService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_MINUTES = 15;
    private static final String INVALID_CREDENTIALS = "Invalid username or password.";

    private final CustomerDAO customerDao;
    private final AdministratorDAO adminDao;
    private final LoginHistoryDAO historyDao;
    private final OtpDAO otpDao;
    private final OTPService otpService = new OTPService();

    /** Lets one login routine work for both customers and admins. */
    @FunctionalInterface
    private interface LoginStateUpdater {
        void update(int failedAttempts, LocalDateTime lockedUntil, LocalDateTime lastLogin);
    }

    public AuthenticationServiceImpl(CustomerDAO customerDao, AdministratorDAO adminDao,
                                     LoginHistoryDAO historyDao, OtpDAO otpDao) {
        this.customerDao = customerDao;
        this.adminDao = adminDao;
        this.historyDao = historyDao;
        this.otpDao = otpDao;
    }

    @Override
    public LoginSession loginCustomer(String username, String password) {
        Optional<Customer> found = customerDao.findByUsername(username);
        if (!found.isPresent()) {
            record(UserType.CUSTOMER, null, username, LoginStatus.FAILED, "Unknown username");
            throw new AuthenticationFailedException(INVALID_CREDENTIALS);
        }
        Customer c = found.get();
        if (c.getAccountStatus() != AccountStatus.ACTIVE) {
            record(UserType.CUSTOMER, c.getCustomerId(), username, LoginStatus.FAILED,
                    "Account " + c.getAccountStatus());
            throw new AuthenticationFailedException(
                    "Your account is " + c.getAccountStatus().name().toLowerCase() + ". Please contact support.");
        }
        LocalDateTime previousLogin = c.getLastLogin();
        LoginStateUpdater updater = (attempts, lockedUntil, lastLogin) ->
                customerDao.updateLoginState(c.getCustomerId(), attempts, lockedUntil, lastLogin);

        long loginId = authenticate(c, UserType.CUSTOMER, c.getCustomerId(), username, password, updater);
        return new LoginSession(UserType.CUSTOMER, c.getCustomerId(), c.getUsername(),
                c.getFullName(), c.getRoleName(), loginId, previousLogin);
    }

    @Override
    public LoginSession loginAdmin(String username, String password) {
        Optional<Administrator> found = adminDao.findByUsername(username);
        if (!found.isPresent()) {
            record(UserType.ADMIN, null, username, LoginStatus.FAILED, "Unknown username");
            throw new AuthenticationFailedException(INVALID_CREDENTIALS);
        }
        Administrator a = found.get();
        if (a.getStatus() != AccountStatus.ACTIVE) {
            record(UserType.ADMIN, a.getAdminId(), username, LoginStatus.FAILED, "Account " + a.getStatus());
            throw new AuthenticationFailedException("This administrator account is not active.");
        }
        LocalDateTime previousLogin = a.getLastLogin();
        LoginStateUpdater updater = (attempts, lockedUntil, lastLogin) ->
                adminDao.updateLoginState(a.getAdminId(), attempts, lockedUntil, lastLogin);

        long loginId = authenticate(a, UserType.ADMIN, a.getAdminId(), username, password, updater);
        return new LoginSession(UserType.ADMIN, a.getAdminId(), a.getUsername(),
                a.getFullName(), a.getRoleName(), loginId, previousLogin);
    }

    @Override
    public void logout(LoginSession session) {
        historyDao.recordLogout(session.getLoginId());
    }

    @Override
    public String requestPasswordReset(String email) {
        Customer c = customerDao.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for that email."));
        otpDao.invalidateAll(c.getCustomerId());
        String code = otpService.generateOtp();
        PasswordResetOtp otp = new PasswordResetOtp();
        otp.setCustomerId(c.getCustomerId());
        otp.setOtpCode(code);
        otp.setExpiresAt(otpService.expiryFromNow());
        otpDao.insert(otp);
        return code;
    }

    @Override
    public void resetPassword(String email, String otpInput, String newPassword) {
        if (!PasswordUtil.isStrong(newPassword)) {
            throw new ValidationException(PasswordUtil.complexityRules());
        }
        Customer c = customerDao.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for that email."));
        PasswordResetOtp otp = otpDao.findLatestUnused(c.getCustomerId())
                .orElseThrow(() -> new AuthenticationFailedException("No active OTP. Please request a new one."));

        if (otpService.isExpired(otp.getExpiresAt())) {
            otpDao.markUsed(otp.getOtpId());
            throw new AuthenticationFailedException("The OTP has expired. Please request a new one.");
        }
        if (!otpService.matches(otp.getOtpCode(), otpInput)) {
            otpDao.markUsed(otp.getOtpId());   // one wrong guess burns the OTP
            throw new AuthenticationFailedException("Incorrect OTP. Please request a new one.");
        }
        customerDao.updatePassword(c.getCustomerId(), PasswordUtil.hash(newPassword));
        otpDao.markUsed(otp.getOtpId());
    }

    // ---------- shared login logic ----------

    private long authenticate(User user, UserType type, int userId, String username,
                              String password, LoginStateUpdater updater) {
        if (user.isLocked()) {
            long minutes = Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes() + 1;
            record(type, userId, username, LoginStatus.LOCKED, "Account temporarily locked");
            throw new AccountLockedException(
                    "Account is temporarily locked. Try again in about " + minutes + " minute(s).");
        }
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() + 1;
            if (attempts >= MAX_ATTEMPTS) {
                updater.update(0, LocalDateTime.now().plusMinutes(LOCK_MINUTES), null);
                record(type, userId, username, LoginStatus.LOCKED, "Too many failed attempts");
                throw new AccountLockedException(
                        "Too many failed attempts. Account locked for " + LOCK_MINUTES + " minutes.");
            }
            updater.update(attempts, null, null);
            record(type, userId, username, LoginStatus.FAILED, "Wrong password");
            throw new AuthenticationFailedException(
                    INVALID_CREDENTIALS + " Attempts left: " + (MAX_ATTEMPTS - attempts));
        }
        updater.update(0, null, LocalDateTime.now());
        return record(type, userId, username, LoginStatus.SUCCESS, null);
    }

    private long record(UserType type, Integer userId, String username, LoginStatus status, String reason) {
        LoginHistory h = new LoginHistory();
        h.setUserType(type);
        h.setCustomerId(type == UserType.CUSTOMER ? userId : null);
        h.setAdminId(type == UserType.ADMIN ? userId : null);
        String safeName = username == null ? "" : username;
        h.setUsernameAttempted(safeName.length() > 50 ? safeName.substring(0, 50) : safeName);
        h.setLoginStatus(status);
        h.setFailureReason(reason);
        return historyDao.insert(h);
    }
}