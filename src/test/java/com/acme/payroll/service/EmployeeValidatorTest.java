package com.acme.payroll.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;
import com.acme.payroll.util.DateUtil;

public class EmployeeValidatorTest {

    private final EmployeeValidator validator = new EmployeeValidator();

    private Employee valid() {
        Employee e = new Employee();
        e.setFirstName("Jane");
        e.setLastName("Doe");
        e.setEmail("jane.doe@acme.test");
        e.setDepartmentId(1L);
        e.setPayType(PayType.SALARIED);
        e.setAnnualSalary(new BigDecimal("50000"));
        e.setHireDate(DateUtil.parse("2020-01-15"));
        return e;
    }

    @Test
    public void validEmployeePasses() {
        assertTrue(validator.validate(valid()).isEmpty());
    }

    @Test
    public void requiredFields() {
        Employee e = new Employee();
        ValidationErrors errors = validator.validate(e);
        assertTrue(errors.has("firstName"));
        assertTrue(errors.has("lastName"));
        assertTrue(errors.has("email"));
        assertTrue(errors.has("departmentId"));
        assertTrue(errors.has("payType"));
        assertTrue(errors.has("hireDate"));
    }

    @Test
    public void rejectsBadEmail() {
        Employee e = valid();
        e.setEmail("jane.doe@");
        assertTrue(validator.validate(e).has("email"));
    }

    @Test
    public void salaryMustBePositive() {
        Employee e = valid();
        e.setAnnualSalary(BigDecimal.ZERO);
        assertTrue(validator.validate(e).has("annualSalary"));
    }

    @Test
    public void hourlyNeedsRateNotSalary() {
        Employee e = valid();
        e.setPayType(PayType.HOURLY);
        e.setAnnualSalary(null);
        assertTrue(validator.validate(e).has("hourlyRate"));
        e.setHourlyRate(new BigDecimal("18.50"));
        assertFalse(validator.validate(e).has("hourlyRate"));
    }

    @Test
    public void rejectsHireDateInFuture() {
        Employee e = valid();
        e.setHireDate(DateUtil.addDays(DateUtil.today(), 30));
        assertTrue(validator.validate(e).has("hireDate"));
    }
}
