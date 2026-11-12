package com.acme.payroll.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.acme.payroll.model.Payslip;

public class PayslipDao extends BaseDao {

    private static final String SELECT =
            "SELECT p.*, e.first_name || ' ' || e.last_name AS employee_name, e.employee_no, "
            + " d.name AS department_name, r.period_start, r.period_end, r.pay_date "
            + "FROM payslip p "
            + "JOIN employee e ON e.id = p.employee_id "
            + "JOIN department d ON d.id = e.department_id "
            + "JOIN pay_run r ON r.id = p.pay_run_id ";

    private static final RowMapper<Payslip> MAPPER = new RowMapper<Payslip>() {
        @Override
        public Payslip map(ResultSet rs) throws SQLException {
            Payslip p = new Payslip();
            p.setId(rs.getLong("id"));
            p.setPayRunId(rs.getLong("pay_run_id"));
            p.setEmployeeId(rs.getLong("employee_id"));
            p.setEmployeeName(rs.getString("employee_name"));
            p.setEmployeeNo(rs.getString("employee_no"));
            p.setDepartmentName(rs.getString("department_name"));
            p.setPeriodStart(rs.getDate("period_start"));
            p.setPeriodEnd(rs.getDate("period_end"));
            p.setPayDate(rs.getDate("pay_date"));
            p.setRegularHours(rs.getBigDecimal("regular_hours"));
            p.setOvertimeHours(rs.getBigDecimal("overtime_hours"));
            p.setGross(rs.getBigDecimal("gross"));
            p.setIncomeTax(rs.getBigDecimal("income_tax"));
            p.setSocial(rs.getBigDecimal("social"));
            p.setPension(rs.getBigDecimal("pension"));
            p.setOtherDeductions(rs.getBigDecimal("other_deductions"));
            p.setNet(rs.getBigDecimal("net"));
            return p;
        }
    };

    public PayslipDao() {
        super();
    }

    public PayslipDao(DataSource ds) {
        super(ds);
    }

    public List<Payslip> findByPayRun(Long payRunId) {
        return query(SELECT + "WHERE p.pay_run_id = ? ORDER BY d.name, e.last_name", MAPPER, payRunId);
    }

    public List<Payslip> findByEmployee(Long employeeId) {
        return query(SELECT + "WHERE p.employee_id = ? ORDER BY r.period_start DESC", MAPPER, employeeId);
    }

    public Payslip findById(Long id) {
        return queryOne(SELECT + "WHERE p.id = ?", MAPPER, id);
    }

    public void deleteByPayRun(Connection con, Long payRunId) throws SQLException {
        update(con, "DELETE FROM payslip WHERE pay_run_id = ?", payRunId);
    }

    public Long create(Connection con, Payslip p) throws SQLException {
        Long id = insert(con, "INSERT INTO payslip (pay_run_id, employee_id, regular_hours, overtime_hours, gross, "
                + "income_tax, social, pension, other_deductions, net) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getPayRunId(), p.getEmployeeId(), p.getRegularHours(), p.getOvertimeHours(), p.getGross(),
                p.getIncomeTax(), p.getSocial(), p.getPension(), p.getOtherDeductions(), p.getNet());
        p.setId(id);
        return id;
    }
}
