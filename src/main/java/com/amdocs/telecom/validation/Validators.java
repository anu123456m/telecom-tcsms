package com.amdocs.telecom.validation;

import java.time.LocalDate;
import java.time.Period;
import java.util.function.Predicate;

public final class Validators {

    public static final int MIN_AGE = 18;

    public static final Predicate<String> NOT_BLANK =
            s -> s != null && !s.trim().isEmpty();

    public static final Predicate<String> VALID_EMAIL =
            s -> s != null && s.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static final Predicate<String> VALID_MOBILE =
            s -> s != null && s.matches("^\\+?[0-9][0-9\\-]{8,18}$");

    public static final Predicate<String> VALID_USERNAME =
            s -> s != null && s.matches("^[A-Za-z0-9_]{4,30}$");

    private Validators() {
    }

    public static boolean isAdult(LocalDate dateOfBirth) {
        return dateOfBirth != null
                && !dateOfBirth.isAfter(LocalDate.now())
                && Period.between(dateOfBirth, LocalDate.now()).getYears() >= MIN_AGE;
    }
}