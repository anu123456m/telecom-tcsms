package com.amdocs.telecom.service.impl;

import com.amdocs.telecom.dao.AddOnDAO;
import com.amdocs.telecom.dao.PlanDAO;
import com.amdocs.telecom.dao.SimDAO;
import com.amdocs.telecom.dao.SubscriptionDAO;
import com.amdocs.telecom.dao.SubscriptionHistoryDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.exception.DuplicateResourceException;
import com.amdocs.telecom.exception.ResourceNotFoundException;
import com.amdocs.telecom.exception.ValidationException;
import com.amdocs.telecom.model.AddOn;
import com.amdocs.telecom.model.MobileSubscription;
import com.amdocs.telecom.model.PlanStatus;
import com.amdocs.telecom.model.PlanType;
import com.amdocs.telecom.model.SIMCard;
import com.amdocs.telecom.model.SimType;
import com.amdocs.telecom.model.SubscriptionAddOn;
import com.amdocs.telecom.model.SubscriptionHistory;
import com.amdocs.telecom.model.SubscriptionStatus;
import com.amdocs.telecom.model.TelecomPlan;
import com.amdocs.telecom.service.SubscriptionService;
import com.amdocs.telecom.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SubscriptionServiceImpl implements SubscriptionService {

    private final PlanDAO planDao;
    private final SimDAO simDao;
    private final SubscriptionDAO subscriptionDao;
    private final SubscriptionHistoryDAO historyDao;
    private final AddOnDAO addOnDao;

    public SubscriptionServiceImpl(PlanDAO planDao, SimDAO simDao, SubscriptionDAO subscriptionDao,
                                   SubscriptionHistoryDAO historyDao, AddOnDAO addOnDao) {
        this.planDao = planDao;
        this.simDao = simDao;
        this.subscriptionDao = subscriptionDao;
        this.historyDao = historyDao;
        this.addOnDao = addOnDao;
    }

    // ---------- subscribe (one JDBC transaction) ----------

    @Override
    public MobileSubscription subscribe(int customerId, String planCode, SimType simType,
                                        boolean byAdmin, String changedBy) {
        TelecomPlan plan = findSelectablePlan(planCode, byAdmin);
        if (simType == null) {
            throw new ValidationException("Choose a SIM type.");
        }
        if (subscriptionDao.hasLiveSubscriptionToPlan(customerId, plan.getPlanId())) {
            throw new DuplicateResourceException("You already have a subscription to " + plan.getPlanName() + ".");
        }

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);

            SIMCard sim = simDao.lockFirstAvailable(con, simType)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No " + simType + " is available right now. Please try again later."));
            int sequence = subscriptionDao.nextSequence(con);

            MobileSubscription s = new MobileSubscription();
            s.setSubscriptionNumber("SUB" + (100000 + sequence));
            s.setCustomerId(customerId);
            s.setPlanId(plan.getPlanId());
            s.setSimId(sim.getSimId());
            s.setMobileNumber(String.format("+91-9%09d", 100000000 + sequence));
            s.setActivationDate(LocalDate.now());
            s.setSubscriptionType(plan.getPlanType());
            s.setStatus(SubscriptionStatus.ACTIVE);
            s.setSubscriptionId(subscriptionDao.insert(con, s));

            simDao.markAssigned(con, sim.getSimId());

            SubscriptionHistory h = new SubscriptionHistory();
            h.setSubscriptionId(s.getSubscriptionId());
            h.setOldPlanId(null);
            h.setNewPlanId(plan.getPlanId());
            h.setChangeReason("New subscription");
            h.setChangedBy(changedBy);
            historyDao.insert(con, h);

            con.commit();

            s.setSimType(sim.getSimType());
            s.setPlanName(plan.getPlanName());
            return s;
        } catch (RuntimeException | SQLException e) {
            rollbackQuietly(con);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new DatabaseException("Could not complete the subscription: " + e.getMessage(), e);
        } finally {
            closeQuietly(con);
        }
    }

    @Override
    public List<MobileSubscription> getSubscriptions(int customerId) {
        return subscriptionDao.findByCustomer(customerId);
    }

    // ---------- upgrade / downgrade (one JDBC transaction) ----------

    @Override
    public String changePlan(int customerId, int subscriptionId, String newPlanCode,
                             boolean byAdmin, String changedBy) {
        MobileSubscription sub = ownedSubscription(customerId, subscriptionId);
        requireActive(sub);

        TelecomPlan newPlan = findSelectablePlan(newPlanCode, byAdmin);
        TelecomPlan oldPlan = planDao.findById(sub.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Current plan not found."));

        if (newPlan.getPlanId() == oldPlan.getPlanId()) {
            throw new ValidationException("You are already on this plan.");
        }
        if (subscriptionDao.hasLiveSubscriptionToPlan(customerId, newPlan.getPlanId())) {
            throw new DuplicateResourceException(
                    "You already have a subscription on " + newPlan.getPlanName() + ".");
        }
        if (!byAdmin && oldPlan.getPlanType() == PlanType.POSTPAID && newPlan.getPlanType() == PlanType.PREPAID) {
            throw new ValidationException("Switching from postpaid to prepaid needs an administrator, "
                    + "because outstanding bills must be cleared first.");
        }

        String kind = newPlan.getMonthlyRental() > oldPlan.getMonthlyRental() ? "UPGRADE"
                : newPlan.getMonthlyRental() < oldPlan.getMonthlyRental() ? "DOWNGRADE" : "CHANGE";

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);

            subscriptionDao.updatePlan(con, subscriptionId, newPlan.getPlanId(), newPlan.getPlanType());

            SubscriptionHistory h = new SubscriptionHistory();
            h.setSubscriptionId(subscriptionId);
            h.setOldPlanId(oldPlan.getPlanId());
            h.setNewPlanId(newPlan.getPlanId());
            h.setChangeReason(kind + ": " + oldPlan.getPlanName() + " to " + newPlan.getPlanName());
            h.setChangedBy(changedBy);
            historyDao.insert(con, h);

            con.commit();
            return kind;
        } catch (RuntimeException | SQLException e) {
            rollbackQuietly(con);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new DatabaseException("Could not change the plan: " + e.getMessage(), e);
        } finally {
            closeQuietly(con);
        }
    }

    @Override
    public List<SubscriptionHistory> getHistory(int customerId, int subscriptionId) {
        ownedSubscription(customerId, subscriptionId);
        return historyDao.findBySubscription(subscriptionId);
    }

    // ---------- add-ons ----------

    @Override
    public List<AddOn> listAddOns() {
        return addOnDao.findAllActive();
    }

    @Override
    public List<AddOn> getActiveAddOns(int customerId, int subscriptionId) {
        ownedSubscription(customerId, subscriptionId);
        return addOnDao.findActiveForSubscription(subscriptionId);
    }

    @Override
    public void activateAddOn(int customerId, int subscriptionId, String addOnCode) {
        requireActive(ownedSubscription(customerId, subscriptionId));
        AddOn addOn = findAddOn(addOnCode);
        if (addOn.getStatus() != PlanStatus.ACTIVE) {
            throw new ValidationException("This add-on is no longer available.");
        }
        Optional<SubscriptionAddOn> link = addOnDao.findLink(subscriptionId, addOn.getAddOnId());
        if (link.isPresent()) {
            if (link.get().getStatus() == PlanStatus.ACTIVE) {
                throw new DuplicateResourceException("This add-on is already active.");
            }
            addOnDao.setLinkStatus(subscriptionId, addOn.getAddOnId(), PlanStatus.ACTIVE);
        } else {
            addOnDao.insertLink(subscriptionId, addOn.getAddOnId());
        }
    }

    @Override
    public void deactivateAddOn(int customerId, int subscriptionId, String addOnCode) {
        requireActive(ownedSubscription(customerId, subscriptionId));
        AddOn addOn = findAddOn(addOnCode);
        SubscriptionAddOn link = addOnDao.findLink(subscriptionId, addOn.getAddOnId())
                .filter(l -> l.getStatus() == PlanStatus.ACTIVE)
                .orElseThrow(() -> new ValidationException("This add-on is not active on this subscription."));
        addOnDao.setLinkStatus(subscriptionId, link.getAddOnId(), PlanStatus.INACTIVE);
    }

    // ---------- helpers ----------

    private TelecomPlan findSelectablePlan(String planCode, boolean byAdmin) {
        String code = planCode == null ? "" : planCode.trim().toUpperCase();
        TelecomPlan plan = planDao.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("No plan with code " + code + "."));
        if (plan.getStatus() != PlanStatus.ACTIVE) {
            throw new ValidationException("This plan is no longer available.");
        }
        if (plan.isRestrictedUpgrade() && !byAdmin) {
            throw new ValidationException(plan.getPlanName()
                    + " is available through an administrator only. Please contact support.");
        }
        return plan;
    }

    private AddOn findAddOn(String addOnCode) {
        String code = addOnCode == null ? "" : addOnCode.trim().toUpperCase();
        return addOnDao.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("No add-on with code " + code + "."));
    }

    private MobileSubscription ownedSubscription(int customerId, int subscriptionId) {
        return subscriptionDao.findById(subscriptionId)
                .filter(s -> s.getCustomerId() == customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found."));
    }

    private void requireActive(MobileSubscription sub) {
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new ValidationException("This subscription is " + sub.getStatus()
                    + ", so changes are not allowed. Please contact support.");
        }
    }

    private void rollbackQuietly(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
                // nothing more we can do
            }
        }
    }

    private void closeQuietly(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignored) {
                // nothing more we can do
            }
        }
    }
}