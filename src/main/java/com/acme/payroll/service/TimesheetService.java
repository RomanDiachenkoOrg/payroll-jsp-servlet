package com.acme.payroll.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.acme.payroll.dao.AuditLogDao;
import com.acme.payroll.dao.EmployeeDao;
import com.acme.payroll.dao.TimesheetDao;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.Role;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.model.TimesheetEntry;
import com.acme.payroll.model.TimesheetStatus;
import com.acme.payroll.model.User;
import com.acme.payroll.util.DateUtil;

public class TimesheetService {

    private static final BigDecimal MAX_DAY = new BigDecimal("24");

    private final TimesheetDao timesheetDao;
    private final EmployeeDao employeeDao;
    private final AuditLogDao auditDao;

    public TimesheetService() {
        this(new TimesheetDao(), new EmployeeDao(), new AuditLogDao());
    }

    public TimesheetService(TimesheetDao timesheetDao, EmployeeDao employeeDao, AuditLogDao auditDao) {
        this.timesheetDao = timesheetDao;
        this.employeeDao = employeeDao;
        this.auditDao = auditDao;
    }

    /** Timesheets visible to the user: own (employee), own department (manager) or all (admin). */
    public List<Timesheet> listFor(User user, TimesheetStatus status) {
        if (user.getRole() == Role.EMPLOYEE) {
            return timesheetDao.find(status, user.getEmployeeId(), null);
        }
        if (user.getRole() == Role.MANAGER) {
            return timesheetDao.find(status, null, user.getDepartmentId());
        }
        return timesheetDao.find(status, null, null);
    }

    public boolean canView(User user, Timesheet t) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        if (user.getRole() == Role.MANAGER) {
            return t.getDepartmentId().equals(user.getDepartmentId());
        }
        return t.getEmployeeId().equals(user.getEmployeeId());
    }

    public boolean canDecide(User user, Timesheet t) {
        if (t.getStatus() != TimesheetStatus.SUBMITTED) {
            return false;
        }
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        return user.getRole() == Role.MANAGER && t.getDepartmentId().equals(user.getDepartmentId())
                && !t.getEmployeeId().equals(user.getEmployeeId());
    }

    public boolean canEdit(User user, Timesheet t) {
        if (!t.isEditable()) {
            return false;
        }
        return user.getRole() == Role.ADMIN || t.getEmployeeId().equals(user.getEmployeeId());
    }

    /** Validates hours (0..24 per day) and the week start, then saves. */
    public ValidationErrors save(Timesheet t, boolean submit, User user) {
        ValidationErrors errors = new ValidationErrors();
        if (t.getEmployeeId() == null) {
            errors.add("employeeId", "Select an employee.");
        } else {
            Employee e = employeeDao.findById(t.getEmployeeId());
            if (e == null || !e.isActive()) {
                errors.add("employeeId", "Employee is not active.");
            }
        }
        if (t.getWeekStart() == null) {
            errors.add("weekStart", "Week start is required.");
        } else if (!DateUtil.isMonday(t.getWeekStart())) {
            errors.add("weekStart", "Week must start on a Monday.");
        } else if (t.getId() == null && t.getEmployeeId() != null
                && timesheetDao.findByEmployeeAndWeek(t.getEmployeeId(), t.getWeekStart()) != null) {
            errors.add("weekStart", "A timesheet for this week already exists.");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < t.getEntries().size(); i++) {
            TimesheetEntry entry = t.getEntries().get(i);
            BigDecimal h = entry.getHours();
            if (h == null) {
                errors.add("hours" + i, "Enter a number of hours.");
            } else if (h.signum() < 0 || h.compareTo(MAX_DAY) > 0) {
                errors.add("hours" + i, "Hours must be between 0 and 24.");
            } else {
                total = total.add(h);
            }
        }
        if (submit && errors.isEmpty() && total.signum() == 0) {
            errors.add("hours0", "Cannot submit an empty timesheet.");
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        boolean isNew = t.getId() == null;
        t.setStatus(TimesheetStatus.DRAFT);
        timesheetDao.saveWithEntries(t);
        if (submit) {
            timesheetDao.markSubmitted(t.getId());
        }
        auditDao.log(user.getEmail(), isNew ? "CREATE" : "UPDATE", "Timesheet", t.getId(),
                (submit ? "Submitted" : "Saved") + " timesheet for week " + DateUtil.format(t.getWeekStart()));
        return errors;
    }

    public void submit(Long id, User user) {
        Timesheet t = timesheetDao.findById(id);
        if (t == null || !canEdit(user, t)) {
            throw new BusinessException("Timesheet cannot be submitted.");
        }
        if (t.getTotalHours().signum() == 0) {
            throw new BusinessException("Cannot submit an empty timesheet.");
        }
        timesheetDao.markSubmitted(id);
        auditDao.log(user.getEmail(), "SUBMIT", "Timesheet", id, "Submitted timesheet");
    }

    public void approve(Long id, User user) {
        Timesheet t = timesheetDao.findById(id);
        if (t == null || !canDecide(user, t)) {
            throw new BusinessException("You cannot approve this timesheet.");
        }
        timesheetDao.decide(id, TimesheetStatus.APPROVED, user.getEmail(), null);
        auditDao.log(user.getEmail(), "APPROVE", "Timesheet", id, "Approved timesheet of " + t.getEmployeeName()
                + " for week " + DateUtil.format(t.getWeekStart()));
    }

    public void reject(Long id, String reason, User user) {
        Timesheet t = timesheetDao.findById(id);
        if (t == null || !canDecide(user, t)) {
            throw new BusinessException("You cannot reject this timesheet.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("A reason is required to reject a timesheet.");
        }
        timesheetDao.decide(id, TimesheetStatus.REJECTED, user.getEmail(), reason.trim());
        auditDao.log(user.getEmail(), "REJECT", "Timesheet", id, "Rejected: " + reason.trim());
    }

    /** A blank Monday-to-Sunday timesheet. */
    public static Timesheet blank(Long employeeId, Date weekStart) {
        Timesheet t = new Timesheet();
        t.setEmployeeId(employeeId);
        t.setWeekStart(weekStart);
        t.setStatus(TimesheetStatus.DRAFT);
        for (int i = 0; i < 7; i++) {
            TimesheetEntry e = new TimesheetEntry();
            e.setWorkDate(DateUtil.addDays(weekStart, i));
            e.setHours(BigDecimal.ZERO);
            t.getEntries().add(e);
        }
        return t;
    }
}
