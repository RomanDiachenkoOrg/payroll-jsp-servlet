package com.acme.payroll.model;

/**
 * Lifecycle of a pay run: DRAFT -> CALCULATED -> APPROVED -> PAID.
 * A PAID run is locked and can no longer be changed.
 */
public enum PayRunStatus {
    DRAFT, CALCULATED, APPROVED, PAID
}
