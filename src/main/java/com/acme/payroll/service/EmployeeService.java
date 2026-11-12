package com.acme.payroll.service;

import java.math.BigDecimal;

import com.acme.payroll.dao.AuditLogDao;
import com.acme.payroll.dao.EmployeeDao;
import com.acme.payroll.dao.SalaryHistoryDao;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;
import com.acme.payroll.model.SalaryHistory;
import com.acme.payroll.util.DateUtil;

public class EmployeeService {

    private final EmployeeDao employeeDao;
    private final SalaryHistoryDao salaryDao;
    private final AuditLogDao auditDao;
    private final EmployeeValidator validator = new EmployeeValidator();

    public EmployeeService() {
        this(new EmployeeDao(), new SalaryHistoryDao(), new AuditLogDao());
    }

    public EmployeeService(EmployeeDao employeeDao, SalaryHistoryDao salaryDao, AuditLogDao auditDao) {
        this.employeeDao = employeeDao;
        this.salaryDao = salaryDao;
        this.auditDao = auditDao;
    }

    /** Validates and saves. Returns the errors; the employee is saved only if there are none. */
    public ValidationErrors save(Employee e, String user) {
        ValidationErrors errors = validator.validate(e);
        if (!errors.has("email")) {
            Employee existing = employeeDao.findByEmail(e.getEmail().trim());
            if (existing != null && !existing.getId().equals(e.getId())) {
                errors.add("email", "Another employee already uses this email.");
            }
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        e.setEmail(e.getEmail().trim());
        if (e.getId() == null) {
            e.setActive(true);
            employeeDao.create(e);
            SalaryHistory h = new SalaryHistory();
            h.setEmployeeId(e.getId());
            h.setEffectiveDate(e.getHireDate());
            h.setAnnualSalary(e.getPayType() == PayType.SALARIED ? e.getAnnualSalary() : null);
            h.setHourlyRate(e.getPayType() == PayType.HOURLY ? e.getHourlyRate() : null);
            h.setReason(e.getPayType() == PayType.SALARIED ? "Starting salary" : "Starting rate");
            h.setCreatedBy(user);
            salaryDao.create(h);
            auditDao.log(user, "CREATE", "Employee", e.getId(), "Created employee " + e.getEmployeeNo()
                    + " " + e.getFullName());
        } else {
            employeeDao.save(e);
            auditDao.log(user, "UPDATE", "Employee", e.getId(), "Updated employee " + e.getEmployeeNo());
        }
        return errors;
    }

    public void deactivate(Long id, String user) {
        Employee e = employeeDao.findById(id);
        if (e == null) {
            throw new BusinessException("Employee not found.");
        }
        if (!e.isActive()) {
            throw new BusinessException("Employee is already inactive.");
        }
        employeeDao.setActive(id, false);
        auditDao.log(user, "UPDATE", "Employee", id, "Deactivated employee " + e.getEmployeeNo());
    }

    public void reactivate(Long id, String user) {
        Employee e = employeeDao.findById(id);
        if (e == null) {
            throw new BusinessException("Employee not found.");
        }
        employeeDao.setActive(id, true);
        auditDao.log(user, "UPDATE", "Employee", id, "Reactivated employee " + e.getEmployeeNo());
    }

    /** Records a raise and updates the employee's current pay. */
    public ValidationErrors addRaise(Long employeeId, BigDecimal amount, java.util.Date effective, String reason,
            String user) {
        ValidationErrors errors = new ValidationErrors();
        Employee e = employeeDao.findById(employeeId);
        if (e == null) {
            throw new BusinessException("Employee not found.");
        }
        if (amount == null || amount.signum() <= 0) {
            errors.add("amount", "New amount must be greater than 0.");
        }
        if (effective == null) {
            errors.add("effectiveDate", "Effective date is required.");
        } else if (effective.before(e.getHireDate())) {
            errors.add("effectiveDate", "Effective date cannot be before the hire date.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            errors.add("reason", "Reason is required.");
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        SalaryHistory h = new SalaryHistory();
        h.setEmployeeId(employeeId);
        h.setEffectiveDate(effective);
        h.setReason(reason.trim());
        h.setCreatedBy(user);
        if (e.isHourly()) {
            h.setHourlyRate(amount);
            employeeDao.updatePay(employeeId, null, amount);
        } else {
            h.setAnnualSalary(amount);
            employeeDao.updatePay(employeeId, amount, null);
        }
        salaryDao.create(h);
        auditDao.log(user, "RAISE", "Employee", employeeId, "New " + (e.isHourly() ? "rate " : "salary ")
                + amount.toPlainString() + " effective " + DateUtil.format(effective));
        return errors;
    }
}
