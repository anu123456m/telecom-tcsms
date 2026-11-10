package com.amdocs.telecom.service;

import com.amdocs.telecom.model.TelecomPlan;

public interface DiscountStrategy {
    double discountFor(TelecomPlan plan, double subtotal);
}