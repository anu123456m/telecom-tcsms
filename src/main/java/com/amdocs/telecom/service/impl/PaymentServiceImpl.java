package com.amdocs.telecom.service.impl;

import com.amdocs.telecom.dao.AuditDAO;
import com.amdocs.telecom.dao.BillDAO;
import com.amdocs.telecom.dao.NotificationDAO;
import com.amdocs.telecom.dao.PaymentDAO;
import com.amdocs.telecom.exception.DatabaseException;
import com.amdocs.telecom.exception.PaymentException;
import com.amdocs.telecom.exception.ResourceNotFoundException;
import com.amdocs.telecom.exception.TcsmsException;
import com.amdocs.telecom.exception.ValidationException;
import com.amdocs.telecom.model.ActorType;
import com.amdocs.telecom.model.AuditLog;
import com.amdocs.telecom.model.Bill;
import com.amdocs.telecom.model.BillStatus;
import com.amdocs.telecom.model.Notification;
import com.amdocs.telecom.model.NotificationStatus;
import com.amdocs.telecom.model.NotificationType;
import com.amdocs.telecom.model.Payment;
import com.amdocs.telecom.model.PaymentMode;
import com.amdocs.telecom.model.PaymentStatus;
import com.amdocs.telecom.service.PaymentService;
import com.amdocs.telecom.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOG = Logger.getLogger(PaymentServiceImpl.class.getName());

    private static final Supplier<String> REFERENCE =
            () -> "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

    private final BillDAO billDao;
    private final PaymentDAO paymentDao;
    private final AuditDAO auditDao;
    private final NotificationDAO notificationDao;

    public PaymentServiceImpl(BillDAO billDao, PaymentDAO paymentDao, AuditDAO auditDao,
                              NotificationDAO notificationDao) {
        this.billDao = billDao;
        this.paymentDao = paymentDao;
        this.auditDao = auditDao;
        this.notificationDao = notificationDao;
    }

    @Override
    public List<Bill> getPayableBills(int customerId) {
        return billDao.findByCustomer(customerId).stream()
                .filter(b -> b.getBillStatus() == BillStatus.GENERATED || b.getBillStatus() == BillStatus.OVERDUE)
                .collect(Collectors.toList());
    }

    @Override
    public Payment pay(int customerId, int billId, double amount, PaymentMode mode) {
        if (mode == null) {
            throw new ValidationException("Choose a payment mode.");
        }
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);

            // 1. validate the bill (row is locked until commit or rollback)
            Bill bill = billDao.findByIdForUpdate(con, billId)
                    .filter(b -> b.getCustomerId() == customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bill not found."));
            if (bill.getBillStatus() == BillStatus.PAID || paymentDao.hasSuccessfulPayment(con, billId)) {
                throw new PaymentException("This bill is already paid. Duplicate payments are not allowed.");
            }
            if (bill.getBillStatus() == BillStatus.CANCELLED) {
                throw new PaymentException("This bill was cancelled and cannot be paid.");
            }

            // 2. validate the amount
            if (Math.abs(amount - bill.getTotalAmount()) > 0.005) {
                throw new PaymentException(String.format(
                        "Amount must be exactly Rs.%.2f for this bill (you entered Rs.%.2f).",
                        bill.getTotalAmount(), amount));
            }

            // 3. create the payment
            Payment p = new Payment();
            p.setTransactionReference(REFERENCE.get());
            p.setBillId(billId);
            p.setCustomerId(customerId);
            p.setAmount(bill.getTotalAmount());
            p.setPaymentMode(mode);
            p.setPaymentStatus(PaymentStatus.SUCCESS);
            p.setPaymentId(paymentDao.insert(con, p));

            // 4. update the bill status
            billDao.updateStatus(con, billId, BillStatus.PAID);

            // 5. create the audit record
            AuditLog log = new AuditLog();
            log.setActorType(ActorType.CUSTOMER);
            log.setActorId(customerId);
            log.setAction("PAYMENT_SUCCESS");
            log.setEntityType("BILL");
            log.setEntityId(billId);
            log.setDetails(String.format("%s paid Rs.%.2f by %s, ref %s",
                    bill.getBillNumber(), bill.getTotalAmount(), mode, p.getTransactionReference()));
            auditDao.insert(con, log);

            // 6. savepoint: if only the notification fails, keep the payment
            Savepoint afterAudit = con.setSavepoint("afterAudit");
            try {
                Notification n = new Notification();
                n.setCustomerId(customerId);
                n.setNotificationType(NotificationType.PAYMENT);
                n.setMessage(String.format("Payment of Rs.%.2f received for bill %s. Ref %s.",
                        bill.getTotalAmount(), bill.getBillNumber(), p.getTransactionReference()));
                n.setStatus(NotificationStatus.PENDING);
                notificationDao.insert(con, n);
            } catch (RuntimeException e) {
                con.rollback(afterAudit);
                LOG.warning("Payment saved, but the notification could not be queued: " + e.getMessage());
            }

            con.commit();
            p.setBillNumber(bill.getBillNumber());
            return p;
        } catch (TcsmsException e) {
            rollbackQuietly(con);
            if (e instanceof DatabaseException) {
                throw new PaymentException("Payment failed and was rolled back. Nothing was charged. ("
                        + e.getMessage() + ")", e);
            }
            throw e;
        } catch (RuntimeException e) {
            rollbackQuietly(con);
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(con);
            throw new PaymentException("Payment failed and was rolled back. Nothing was charged.", e);
        } finally {
            closeQuietly(con);
        }
    }

    @Override
    public List<Payment> getPaymentsForCustomer(int customerId) {
        return paymentDao.findByCustomer(customerId);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentDao.findAllDetailed();
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