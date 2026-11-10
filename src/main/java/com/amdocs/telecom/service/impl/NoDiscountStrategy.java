package com.amdocs.telecom.service.impl;

import com.amdocs.telecom.model.TelecomPlan;
import com.amdocs.telecom.service.DiscountStrategy;

public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public double discountFor(TelecomPlan plan, double subtotal) {
        return 0.0;
    }
}