package com.acme.payroll.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acme.payroll.dao.AuditLogDao;
import com.acme.payroll.dao.DaoException;
import com.acme.payroll.dao.DeductionDao;
import com.acme.payroll.dao.EmployeeDao;
import com.acme.payroll.dao.PayRunDao;
import com.acme.payroll.dao.PayslipDao;
import com.acme.payroll.dao.SettingsDao;
import com.acme.payroll.dao.TimesheetDao;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayRun;
import com.acme.payroll.model.PayRunStatus;
import com.acme.payroll.model.PayType;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.util.Database;
import com.acme.payroll.util.DateUtil;
import com.acme.payroll.util.JdbcUtil;

/**
 * Pay run workflow: DRAFT -> CALCULATED -> APPROVED -> PAID.
 */
public class PayRunService {

    private static final Logger log = LoggerFactory.getLogger(PayRunService.class);

    private final DataSource dataSource;
    private final PayRunDao payRunDao;
    private final PayslipDao payslipDao;
    private final EmployeeDao employeeDao;
    private final TimesheetDao timesheetDao;
    private final DeductionDao deductionDao;
    private final SettingsDao settingsDao;
    private final AuditLogDao auditDao;

    public PayRunService() {
        this(Database.getDataSource());
    }

    public PayRunService(DataSource ds) {
        this.dataSource = ds;
        this.payRunDao = new PayRunDao(ds);
        this.payslipDao = new PayslipDao(ds);
        this.employeeDao = new EmployeeDao(ds);
        this.timesheetDao = new TimesheetDao(ds);
        this.deductionDao = new DeductionDao(ds);
        this.settingsDao = new SettingsDao(ds);
        this.auditDao = new AuditLogDao(ds);
    }

    public PayRun create(Date periodStart, Date periodEnd, Date payDate, String user) {
        if (periodStart == null || periodEnd == null || payDate == null) {
            throw new BusinessException("Period start, period end and pay date are required.");
        }
        if (periodEnd.before(periodStart)) {
            throw new BusinessException("Period end must be on or after period start.");
        }
        if (payDate.before(periodStart)) {
            throw new BusinessException("Pay date cannot be before the period start.");
        }
        if (payRunDao.overlaps(periodStart, periodEnd)) {
            throw new BusinessException("A pay run already exists for an overlapping period.");
        }
        PayRun run = new PayRun();
        run.setPeriodStart(periodStart);
        run.setPeriodEnd(periodEnd);
        run.setPayDate(payDate);
        run.setCreatedBy(user);
        payRunDao.create(run);
        auditDao.log(user, "CREATE", "PayRun", run.getId(), "Created pay run "
                + DateUtil.format(periodStart) + ".." + DateUtil.format(periodEnd));
        return run;
    }

    public PayrollCalculator newCalculator() {
        return new PayrollCalculator(settingsDao.findBrackets(),
                settingsDao.getDecimal(SettingsDao.SOCIAL_RATE, "0.05"),
                settingsDao.getDecimal(SettingsDao.PENSION_RATE, "0.03"));
    }

    /** (Re)calculates all payslips of a pay run. */
    public int calculate(Long payRunId, String user) {
        PayRun run = load(payRunId);
        if (run.getStatus() != PayRunStatus.DRAFT && run.getStatus() != PayRunStatus.CALCULATED) {
            throw new BusinessException("Only draft or calculated pay runs can be calculated.");
        }
        PayrollCalculator calc = newCalculator();
        Connection con = null;
        int count = 0;
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            payslipDao.deleteByPayRun(con, payRunId);
            List<Employee> employees = employeeDao.findActiveForPeriod(con, run.getPeriodEnd());
            for (Employee e : employees) {
                List<Timesheet> sheets = java.util.Collections.emptyList();
                if (e.getPayType() == PayType.HOURLY) {
                    sheets = timesheetDao.findApprovedForPeriod(con, e.getId(), run.getPeriodStart(),
                            run.getPeriodEnd());
                    if (sheets.isEmpty()) {
                        continue;
                    }
                }
                BigDecimal other = deductionDao.totalForEmployee(con, e.getId());
                Payslip p = calc.calculate(e, sheets, other);
                p.setPayRunId(payRunId);
                payslipDao.create(con, p);
                count++;
            }
            payRunDao.markCalculated(con, payRunId);
            con.commit();
        } catch (SQLException ex) {
            JdbcUtil.rollbackQuietly(con);
            throw new DaoException("Pay run calculation failed", ex);
        } finally {
            JdbcUtil.close(null, null, con);
        }
        log.info("Pay run {} calculated: {} payslips", payRunId, count);
        auditDao.log(user, "CALCULATE", "PayRun", payRunId, "Calculated " + count + " payslips");
        return count;
    }

    public void approve(Long payRunId, String user) {
        PayRun run = load(payRunId);
        if (run.getStatus() != PayRunStatus.CALCULATED) {
            throw new BusinessException("Only calculated pay runs can be approved.");
        }
        if (run.getPayslipCount() == 0) {
            throw new BusinessException("Pay run has no payslips.");
        }
        payRunDao.markApproved(payRunId, user);
        auditDao.log(user, "APPROVE", "PayRun", payRunId, "Approved pay run");
    }

    public void markPaid(Long payRunId, String user) {
        PayRun run = load(payRunId);
        if (run.isLocked()) {
            throw new BusinessException("Pay run is already paid.");
        }
        if (run.getStatus() == PayRunStatus.DRAFT) {
            throw new BusinessException("Pay run must be calculated and approved first.");
        }
        payRunDao.markPaid(payRunId);
        auditDao.log(user, "MARK_PAID", "PayRun", payRunId, "Pay run marked as paid");
    }

    public void delete(Long payRunId, String user) {
        PayRun run = load(payRunId);
        if (run.getStatus() != PayRunStatus.DRAFT && run.getStatus() != PayRunStatus.CALCULATED) {
            throw new BusinessException("Approved or paid pay runs cannot be deleted.");
        }
        payRunDao.delete(payRunId);
        auditDao.log(user, "DELETE", "PayRun", payRunId, "Deleted pay run "
                + DateUtil.format(run.getPeriodStart()) + ".." + DateUtil.format(run.getPeriodEnd()));
    }

    private PayRun load(Long id) {
        PayRun run = payRunDao.findById(id);
        if (run == null) {
            throw new BusinessException("Pay run not found.");
        }
        return run;
    }
}
