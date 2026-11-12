package com.acme.payroll.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import com.acme.payroll.model.PayRun;
import com.acme.payroll.model.PayRunStatus;
import com.acme.payroll.util.DateUtil;

public class PayRunDao extends BaseDao {

    private static final String SELECT =
            "SELECT r.*, "
            + " (SELECT COUNT(*) FROM payslip p WHERE p.pay_run_id = r.id) AS payslip_count, "
            + " (SELECT COALESCE(SUM(p.gross), 0) FROM payslip p WHERE p.pay_run_id = r.id) AS total_gross, "
            + " (SELECT COALESCE(SUM(p.net), 0) FROM payslip p WHERE p.pay_run_id = r.id) AS total_net "
            + "FROM pay_run r ";

    private static final RowMapper<PayRun> MAPPER = new RowMapper<PayRun>() {
        @Override
        public PayRun map(ResultSet rs) throws SQLException {
            PayRun r = new PayRun();
            r.setId(rs.getLong("id"));
            r.setPeriodStart(rs.getDate("period_start"));
            r.setPeriodEnd(rs.getDate("period_end"));
            r.setPayDate(rs.getDate("pay_date"));
            r.setStatus(PayRunStatus.valueOf(rs.getString("status")));
            r.setCreatedBy(rs.getString("created_by"));
            r.setCreatedAt(rs.getTimestamp("created_at"));
            r.setCalculatedAt(rs.getTimestamp("calculated_at"));
            r.setApprovedBy(rs.getString("approved_by"));
            r.setApprovedAt(rs.getTimestamp("approved_at"));
            r.setPaidAt(rs.getTimestamp("paid_at"));
            r.setPayslipCount(rs.getInt("payslip_count"));
            r.setTotalGross(rs.getBigDecimal("total_gross"));
            r.setTotalNet(rs.getBigDecimal("total_net"));
            return r;
        }
    };

    public PayRunDao() {
        super();
    }

    public PayRunDao(DataSource ds) {
        super(ds);
    }

    public List<PayRun> findAll() {
        return query(SELECT + "ORDER BY r.period_start DESC", MAPPER);
    }

    public PayRun findById(Long id) {
        return queryOne(SELECT + "WHERE r.id = ?", MAPPER, id);
    }

    public PayRun findLastPaid() {
        return queryOne(SELECT + "WHERE r.status = 'PAID' ORDER BY r.period_end DESC LIMIT 1", MAPPER);
    }

    public PayRun findNextOpen() {
        return queryOne(SELECT + "WHERE r.status <> 'PAID' ORDER BY r.pay_date LIMIT 1", MAPPER);
    }

    public List<PayRun> findRecentPaid(int limit) {
        return query(SELECT + "WHERE r.status = 'PAID' ORDER BY r.period_end DESC LIMIT ?", MAPPER, limit);
    }

    public boolean overlaps(Date from, Date to) {
        return queryInt("SELECT COUNT(*) FROM pay_run WHERE period_start <= ? AND period_end >= ?",
                DateUtil.toSql(to), DateUtil.toSql(from)) > 0;
    }

    public Long create(PayRun r) {
        Long id = insert("INSERT INTO pay_run (period_start, period_end, pay_date, status, created_by) "
                + "VALUES (?, ?, ?, ?, ?)",
                DateUtil.toSql(r.getPeriodStart()), DateUtil.toSql(r.getPeriodEnd()),
                DateUtil.toSql(r.getPayDate()), PayRunStatus.DRAFT, r.getCreatedBy());
        r.setId(id);
        return id;
    }

    public void markCalculated(Connection con, Long id) throws SQLException {
        update(con, "UPDATE pay_run SET status = 'CALCULATED', calculated_at = ?, approved_by = NULL, "
                + "approved_at = NULL WHERE id = ?", now(), id);
    }

    public void markApproved(Long id, String by) {
        update("UPDATE pay_run SET status = 'APPROVED', approved_by = ?, approved_at = ? WHERE id = ?",
                by, now(), id);
    }

    public void markPaid(Long id) {
        update("UPDATE pay_run SET status = 'PAID', paid_at = ? WHERE id = ?", now(), id);
    }

    public void delete(Long id) {
        update("DELETE FROM pay_run WHERE id = ?", id);
    }
}
