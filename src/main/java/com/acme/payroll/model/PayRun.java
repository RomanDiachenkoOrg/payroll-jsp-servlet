package com.acme.payroll.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * A payroll run for a calendar period.
 */
public class PayRun implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Date periodStart;
    private Date periodEnd;
    private Date payDate;
    private PayRunStatus status;
    private String createdBy;
    private Timestamp createdAt;
    private Timestamp calculatedAt;
    private String approvedBy;
    private Timestamp approvedAt;
    private Timestamp paidAt;
    private int payslipCount;
    private BigDecimal totalGross;
    private BigDecimal totalNet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public PayRunStatus getStatus() {
        return status;
    }

    public void setStatus(PayRunStatus status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Timestamp calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Timestamp getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Timestamp approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }

    public int getPayslipCount() {
        return payslipCount;
    }

    public void setPayslipCount(int payslipCount) {
        this.payslipCount = payslipCount;
    }

    public BigDecimal getTotalGross() {
        return totalGross;
    }

    public void setTotalGross(BigDecimal totalGross) {
        this.totalGross = totalGross;
    }

    public BigDecimal getTotalNet() {
        return totalNet;
    }

    public void setTotalNet(BigDecimal totalNet) {
        this.totalNet = totalNet;
    }

    public boolean isLocked() {
        return status == PayRunStatus.PAID;
    }

    public String getPeriodLabel() {
        return new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.US).format(periodStart);
    }

    @Override
    public String toString() {
        return "PayRun[id=" + id + "]";
    }
}
