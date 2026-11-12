package com.acme.payroll.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * An employee on the payroll.
 */
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String employeeNo;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private String jobTitle;
    private PayType payType;
    private BigDecimal annualSalary;
    private BigDecimal hourlyRate;
    private boolean pensionOptIn;
    private Date hireDate;
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public PayType getPayType() {
        return payType;
    }

    public void setPayType(PayType payType) {
        this.payType = payType;
    }

    public BigDecimal getAnnualSalary() {
        return annualSalary;
    }

    public void setAnnualSalary(BigDecimal annualSalary) {
        this.annualSalary = annualSalary;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public boolean isPensionOptIn() {
        return pensionOptIn;
    }

    public void setPensionOptIn(boolean pensionOptIn) {
        this.pensionOptIn = pensionOptIn;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isHourly() {
        return payType == PayType.HOURLY;
    }

    /** Monthly base for salaried staff, used on the profile page. */
    public BigDecimal getMonthlySalary() {
        if (annualSalary == null) {
            return null;
        }
        return annualSalary.divide(new BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "Employee[id=" + id + "]";
    }
}
