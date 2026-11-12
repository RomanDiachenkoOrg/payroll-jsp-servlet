package com.acme.payroll.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import com.acme.payroll.model.DepartmentCost;
import com.acme.payroll.util.DateUtil;

public class ReportDao extends BaseDao {

    public ReportDao() {
        super();
    }

    public ReportDao(DataSource ds) {
        super(ds);
    }

    /**
     * Payroll cost per department for pay runs whose pay date falls in the given range.
     * Only approved and paid runs are included.
     */
    public List<DepartmentCost> costByDepartment(Date from, Date to) {
        return query("SELECT d.name, COUNT(DISTINCT p.employee_id) AS headcount, "
                + " SUM(p.gross) AS gross, SUM(p.income_tax) AS tax, SUM(p.social) AS social, "
                + " SUM(p.pension) AS pension, SUM(p.net) AS net "
                + "FROM payslip p "
                + "JOIN pay_run r ON r.id = p.pay_run_id "
                + "JOIN employee e ON e.id = p.employee_id "
                + "JOIN department d ON d.id = e.department_id "
                + "WHERE r.status IN ('APPROVED', 'PAID') AND r.pay_date BETWEEN ? AND ? "
                + "GROUP BY d.name ORDER BY d.name",
                new RowMapper<DepartmentCost>() {
                    @Override
                    public DepartmentCost map(ResultSet rs) throws SQLException {
                        DepartmentCost c = new DepartmentCost();
                        c.setDepartmentName(rs.getString("name"));
                        c.setHeadcount(rs.getInt("headcount"));
                        c.setGross(rs.getBigDecimal("gross"));
                        c.setTax(rs.getBigDecimal("tax"));
                        c.setSocial(rs.getBigDecimal("social"));
                        c.setPension(rs.getBigDecimal("pension"));
                        c.setNet(rs.getBigDecimal("net"));
                        return c;
                    }
                }, DateUtil.toSql(from), DateUtil.toSql(to));
    }
}
