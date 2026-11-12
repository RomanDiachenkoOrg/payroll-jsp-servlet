package com.acme.payroll.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.regex.Pattern;

import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;
import com.acme.payroll.util.DateUtil;

/** Field-level validation of the employee form. Uniqueness is checked by the service. */
public class EmployeeValidator {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public ValidationErrors validate(Employee e) {
        ValidationErrors errors = new ValidationErrors();
        if (isBlank(e.getFirstName())) {
            errors.add("firstName", "First name is required.");
        } else if (e.getFirstName().length() > 60) {
            errors.add("firstName", "First name must be at most 60 characters.");
        }
        if (isBlank(e.getLastName())) {
            errors.add("lastName", "Last name is required.");
        } else if (e.getLastName().length() > 60) {
            errors.add("lastName", "Last name must be at most 60 characters.");
        }
        if (isBlank(e.getEmail())) {
            errors.add("email", "Email is required.");
        } else if (!EMAIL.matcher(e.getEmail().trim()).matches()) {
            errors.add("email", "Enter a valid email address.");
        }
        if (e.getDepartmentId() == null) {
            errors.add("departmentId", "Select a department.");
        }
        if (e.getPayType() == null) {
            errors.add("payType", "Select a pay type.");
        } else if (e.getPayType() == PayType.SALARIED) {
            if (!positive(e.getAnnualSalary())) {
                errors.add("annualSalary", "Annual salary must be greater than 0.");
            }
        } else if (!positive(e.getHourlyRate())) {
            errors.add("hourlyRate", "Hourly rate must be greater than 0.");
        }
        Date hire = e.getHireDate();
        if (hire == null) {
            errors.add("hireDate", "Hire date is required (yyyy-mm-dd).");
        } else if (hire.after(DateUtil.today())) {
            errors.add("hireDate", "Hire date cannot be in the future.");
        }
        return errors;
    }

    private static boolean positive(BigDecimal v) {
        return v != null && v.signum() > 0;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
