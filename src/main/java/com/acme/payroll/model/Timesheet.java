package com.acme.payroll.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Weekly timesheet (Monday to Sunday) with one entry per day.
 */
public class Timesheet implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long departmentId;
    private Date weekStart;
    private TimesheetStatus status;
    private Timestamp submittedAt;
    private String decidedBy;
    private Timestamp decidedAt;
    private String rejectReason;
    private BigDecimal totalHours;
    private List<TimesheetEntry> entries = new ArrayList<TimesheetEntry>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Date getWeekStart() { return weekStart; }
    public void setWeekStart(Date weekStart) { this.weekStart = weekStart; }

    public TimesheetStatus getStatus() { return status; }
    public void setStatus(TimesheetStatus status) { this.status = status; }

    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }

    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }

    public Timestamp getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Timestamp decidedAt) { this.decidedAt = decidedAt; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public List<TimesheetEntry> getEntries() { return entries; }
    public void setEntries(List<TimesheetEntry> entries) { this.entries = entries; }

    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }

    public BigDecimal getTotalHours() {
        if (totalHours != null) {
            return totalHours;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (TimesheetEntry e : entries) {
            if (e.getHours() != null) {
                sum = sum.add(e.getHours());
            }
        }
        return sum;
    }

    public boolean isEditable() {
        return status == null || status.isEditable();
    }
}
