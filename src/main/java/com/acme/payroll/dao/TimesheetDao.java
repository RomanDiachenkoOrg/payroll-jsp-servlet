package com.acme.payroll.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import com.acme.payroll.model.Timesheet;
import com.acme.payroll.model.TimesheetEntry;
import com.acme.payroll.model.TimesheetStatus;
import com.acme.payroll.util.DateUtil;
import com.acme.payroll.util.JdbcUtil;

public class TimesheetDao extends BaseDao {

    private static final String SELECT =
            "SELECT t.*, e.first_name || ' ' || e.last_name AS employee_name, e.department_id, "
            + " (SELECT COALESCE(SUM(x.hours), 0) FROM timesheet_entry x WHERE x.timesheet_id = t.id) AS total_hours "
            + "FROM timesheet t JOIN employee e ON e.id = t.employee_id ";

    private static final RowMapper<Timesheet> MAPPER = new RowMapper<Timesheet>() {
        @Override
        public Timesheet map(ResultSet rs) throws SQLException {
            Timesheet t = new Timesheet();
            t.setId(rs.getLong("id"));
            t.setEmployeeId(rs.getLong("employee_id"));
            t.setEmployeeName(rs.getString("employee_name"));
            t.setDepartmentId(rs.getLong("department_id"));
            t.setWeekStart(rs.getDate("week_start"));
            t.setStatus(TimesheetStatus.valueOf(rs.getString("status")));
            t.setSubmittedAt(rs.getTimestamp("submitted_at"));
            t.setDecidedBy(rs.getString("decided_by"));
            t.setDecidedAt(rs.getTimestamp("decided_at"));
            t.setRejectReason(rs.getString("reject_reason"));
            t.setTotalHours(rs.getBigDecimal("total_hours"));
            return t;
        }
    };

    private static final RowMapper<TimesheetEntry> ENTRY_MAPPER = new RowMapper<TimesheetEntry>() {
        @Override
        public TimesheetEntry map(ResultSet rs) throws SQLException {
            TimesheetEntry e = new TimesheetEntry();
            e.setId(rs.getLong("id"));
            e.setTimesheetId(rs.getLong("timesheet_id"));
            e.setWorkDate(rs.getDate("work_date"));
            e.setHours(rs.getBigDecimal("hours"));
            return e;
        }
    };

    public TimesheetDao() {
        super();
    }

    public TimesheetDao(DataSource ds) {
        super(ds);
    }

    /**
     * @param status       null for all statuses
     * @param employeeId   restrict to one employee (employee role), may be null
     * @param departmentId restrict to one department (manager role), may be null
     */
    public List<Timesheet> find(TimesheetStatus status, Long employeeId, Long departmentId) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<Object>();
        if (status != null) {
            sql.append("AND t.status = ? ");
            params.add(status);
        }
        if (employeeId != null) {
            sql.append("AND t.employee_id = ? ");
            params.add(employeeId);
        }
        if (departmentId != null) {
            sql.append("AND e.department_id = ? ");
            params.add(departmentId);
        }
        sql.append("ORDER BY t.week_start DESC, employee_name");
        return query(sql.toString(), MAPPER, params.toArray());
    }

    public Timesheet findById(Long id) {
        Timesheet t = queryOne(SELECT + "WHERE t.id = ?", MAPPER, id);
        if (t != null) {
            t.setEntries(findEntries(id));
            t.setTotalHours(null);
        }
        return t;
    }

    public Timesheet findByEmployeeAndWeek(Long employeeId, Date weekStart) {
        return queryOne(SELECT + "WHERE t.employee_id = ? AND t.week_start = ?", MAPPER,
                employeeId, DateUtil.toSql(weekStart));
    }

    public List<TimesheetEntry> findEntries(Long timesheetId) {
        return query("SELECT * FROM timesheet_entry WHERE timesheet_id = ? ORDER BY work_date",
                ENTRY_MAPPER, timesheetId);
    }

    /** Approved timesheets of an employee whose week starts inside the period, with entries loaded. */
    public List<Timesheet> findApprovedForPeriod(Connection con, Long employeeId, Date from, Date to)
            throws SQLException {
        List<Timesheet> list = query(con, SELECT + "WHERE t.employee_id = ? AND t.status = 'APPROVED' "
                + "AND t.week_start BETWEEN ? AND ? ORDER BY t.week_start",
                MAPPER, employeeId, DateUtil.toSql(from), DateUtil.toSql(to));
        for (Timesheet t : list) {
            t.setEntries(query(con, "SELECT * FROM timesheet_entry WHERE timesheet_id = ? ORDER BY work_date",
                    ENTRY_MAPPER, t.getId()));
        }
        return list;
    }

    public int countByStatus(TimesheetStatus status) {
        return queryInt("SELECT COUNT(*) FROM timesheet WHERE status = ?", status.name());
    }

    /** Inserts or replaces a timesheet together with its daily entries in one transaction. */
    public Long saveWithEntries(Timesheet t) {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            if (t.getId() == null) {
                t.setId(insert(con, "INSERT INTO timesheet (employee_id, week_start, status) VALUES (?, ?, ?)",
                        t.getEmployeeId(), DateUtil.toSql(t.getWeekStart()), t.getStatus()));
            } else {
                update(con, "UPDATE timesheet SET status = ?, reject_reason = NULL WHERE id = ?",
                        t.getStatus(), t.getId());
                update(con, "DELETE FROM timesheet_entry WHERE timesheet_id = ?", t.getId());
            }
            for (TimesheetEntry e : t.getEntries()) {
                BigDecimal h = e.getHours() == null ? BigDecimal.ZERO : e.getHours();
                insert(con, "INSERT INTO timesheet_entry (timesheet_id, work_date, hours) VALUES (?, ?, ?)",
                        t.getId(), DateUtil.toSql(e.getWorkDate()), h);
            }
            con.commit();
            return t.getId();
        } catch (SQLException e) {
            JdbcUtil.rollbackQuietly(con);
            throw new DaoException("Could not save timesheet", e);
        } finally {
            JdbcUtil.close(null, null, con);
        }
    }

    public void markSubmitted(Long id) {
        update("UPDATE timesheet SET status = 'SUBMITTED', submitted_at = ?, reject_reason = NULL WHERE id = ?",
                now(), id);
    }

    public void decide(Long id, TimesheetStatus status, String decidedBy, String reason) {
        update("UPDATE timesheet SET status = ?, decided_by = ?, decided_at = ?, reject_reason = ? WHERE id = ?",
                status, decidedBy, now(), reason, id);
    }

    public void delete(Long id) {
        update("DELETE FROM timesheet WHERE id = ?", id);
    }
}
