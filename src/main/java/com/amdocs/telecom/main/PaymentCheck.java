package com.amdocs.telecom.main;

import com.amdocs.telecom.dao.BillDAO;
import com.amdocs.telecom.dao.CustomerDAO;
import com.amdocs.telecom.dao.impl.AuditDAOImpl;
import com.amdocs.telecom.dao.impl.BillDAOImpl;
import com.amdocs.telecom.dao.impl.CustomerDAOImpl;
import com.amdocs.telecom.dao.impl.NotificationDAOImpl;
import com.amdocs.telecom.dao.impl.PaymentDAOImpl;
import com.amdocs.telecom.exception.TcsmsException;
import com.amdocs.telecom.model.Bill;
import com.amdocs.telecom.model.Customer;
import com.amdocs.telecom.model.Payment;
import com.amdocs.telecom.model.PaymentMode;
import com.amdocs.telecom.service.PaymentService;
import com.amdocs.telecom.service.impl.PaymentServiceImpl;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PaymentCheck {

    public static void main(String[] args) throws Exception {
        BillDAO billDao = new BillDAOImpl();
        CustomerDAO customerDao = new CustomerDAOImpl();
        PaymentService service = new PaymentServiceImpl(billDao, new PaymentDAOImpl(),
                new AuditDAOImpl(), new NotificationDAOImpl());

        Customer arjun = customerDao.findByUsername("arjun")
                .orElseThrow(() -> new IllegalStateException("arjun not found"));
        List<Bill> payable = service.getPayableBills(arjun.getCustomerId());
        if (payable.isEmpty()) {
            System.out.println("arjun has no unpaid bills. Generate a bill for a new month first.");
            return;
        }
        Bill bill = payable.get(0);
        System.out.println("Two threads will now try to pay " + bill.getBillNumber() + " at the same time...");

        Callable<String> task = () -> {
            try {
                Payment p = service.pay(bill.getCustomerId(), bill.getBillId(),
                        bill.getTotalAmount(), PaymentMode.UPI);
                return "SUCCESS " + p.getTransactionReference();
            } catch (TcsmsException e) {
                return "REJECTED: " + e.getMessage();
            }
        };

        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<String> a = pool.submit(task);
        Future<String> b = pool.submit(task);
        System.out.println("Thread A: " + a.get());
        System.out.println("Thread B: " + b.get());
        pool.shutdown();
    }
}