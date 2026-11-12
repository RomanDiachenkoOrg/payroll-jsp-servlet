package com.acme.payroll.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Calculated pay for one employee in one pay run.
 */
public class Payslip implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long payRunId;
    private Long employeeId;
    private String employeeName;
    private String employeeNo;
    private String departmentName;
    private Date periodStart;
    private Date periodEnd;
    private Date payDate;
    private BigDecimal regularHours;
    private BigDecimal overtimeHours;
    private BigDecimal gross;
    private BigDecimal incomeTax;
    private BigDecimal social;
    private BigDecimal pension;
    private BigDecimal otherDeductions;
    private BigDecimal net;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPayRunId() {
        return payRunId;
    }

    public void setPayRunId(Long payRunId) {
        this.payRunId = payRunId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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

    public BigDecimal getRegularHours() {
        return regularHours;
    }

    public void setRegularHours(BigDecimal regularHours) {
        this.regularHours = regularHours;
    }

    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public BigDecimal getGross() {
        return gross;
    }

    public void setGross(BigDecimal gross) {
        this.gross = gross;
    }

    public BigDecimal getIncomeTax() {
        return incomeTax;
    }

    public void setIncomeTax(BigDecimal incomeTax) {
        this.incomeTax = incomeTax;
    }

    public BigDecimal getSocial() {
        return social;
    }

    public void setSocial(BigDecimal social) {
        this.social = social;
    }

    public BigDecimal getPension() {
        return pension;
    }

    public void setPension(BigDecimal pension) {
        this.pension = pension;
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    public void setOtherDeductions(BigDecimal otherDeductions) {
        this.otherDeductions = otherDeductions;
    }

    public BigDecimal getNet() {
        return net;
    }

    public void setNet(BigDecimal net) {
        this.net = net;
    }

    public BigDecimal getTotalDeductions() {
        return incomeTax.add(social).add(pension).add(otherDeductions);
    }

    @Override
    public String toString() {
        return "Payslip[id=" + id + "]";
    }
}
