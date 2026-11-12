package com.acme.payroll.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.acme.payroll.model.SalaryHistory;
import com.acme.payroll.util.DateUtil;

public class SalaryHistoryDao extends BaseDao {

    private static final RowMapper<SalaryHistory> MAPPER = new RowMapper<SalaryHistory>() {
        @Override
        public SalaryHistory map(ResultSet rs) throws SQLException {
            SalaryHistory h = new SalaryHistory();
            h.setId(rs.getLong("id"));
            h.setEmployeeId(rs.getLong("employee_id"));
            h.setEffectiveDate(rs.getDate("effective_date"));
            h.setAnnualSalary(rs.getBigDecimal("annual_salary"));
            h.setHourlyRate(rs.getBigDecimal("hourly_rate"));
            h.setReason(rs.getString("reason"));
            h.setCreatedBy(rs.getString("created_by"));
            return h;
        }
    };

    public SalaryHistoryDao() {
        super();
    }

    public SalaryHistoryDao(DataSource ds) {
        super(ds);
    }

    public List<SalaryHistory> findByEmployee(Long employeeId) {
        return query("SELECT * FROM salary_history WHERE employee_id = ? ORDER BY effective_date DESC, id DESC",
                MAPPER, employeeId);
    }

    public Long create(SalaryHistory h) {
        Long id = insert("INSERT INTO salary_history (employee_id, effective_date, annual_salary, hourly_rate, "
                + "reason, created_by) VALUES (?, ?, ?, ?, ?, ?)",
                h.getEmployeeId(), DateUtil.toSql(h.getEffectiveDate()), h.getAnnualSalary(), h.getHourlyRate(),
                h.getReason(), h.getCreatedBy());
        h.setId(id);
        return id;
    }
}
