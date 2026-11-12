package com.acme.payroll.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    public static final BigDecimal ZERO = new BigDecimal("0.00");

    private Money() {
    }

    /** Rounds to cents, half-up. */
    public static BigDecimal round(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public static BigDecimal parse(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(s.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
