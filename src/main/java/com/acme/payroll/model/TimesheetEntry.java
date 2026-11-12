package com.acme.payroll.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Hours worked on a single day of a timesheet week.
 */
public class TimesheetEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long timesheetId;
    private Date workDate;
    private BigDecimal hours;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimesheetId() {
        return timesheetId;
    }

    public void setTimesheetId(Long timesheetId) {
        this.timesheetId = timesheetId;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    @Override
    public String toString() {
        return "TimesheetEntry[id=" + id + "]";
    }
}
