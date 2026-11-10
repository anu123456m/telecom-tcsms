package com.amdocs.telecom.service.impl;

import com.amdocs.telecom.model.PlanType;
import com.amdocs.telecom.model.TelecomPlan;
import com.amdocs.telecom.service.DiscountStrategy;

/** Rs 50 off postpaid bills of Rs 900 or more (before tax). */
public class PostpaidDiscountStrategy implements DiscountStrategy {
    @Override
    public double discountFor(TelecomPlan plan, double subtotal) {
        return plan.getPlanType() == PlanType.POSTPAID && subtotal >= 900 ? 50.0 : 0.0;
    }
}