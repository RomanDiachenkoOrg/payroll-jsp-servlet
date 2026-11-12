package com.acme.payroll.model;

public enum TimesheetStatus {
    DRAFT, SUBMITTED, APPROVED, REJECTED;

    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }
}
