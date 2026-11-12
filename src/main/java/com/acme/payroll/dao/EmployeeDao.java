package com.acme.payroll.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;
import com.acme.payroll.util.DateUtil;
import com.acme.payroll.util.Page;

public class EmployeeDao extends BaseDao {

    private static final String SELECT =
            "SELECT e.*, d.name AS department_name FROM employee e "
            + "JOIN department d ON d.id = e.department_id ";

    static final RowMapper<Employee> MAPPER = new RowMapper<Employee>() {
        @Override
        public Employee map(ResultSet rs) throws SQLException {
            Employee e = new Employee();
            e.setId(rs.getLong("id"));
            e.setEmployeeNo(rs.getString("employee_no"));
            e.setFirstName(rs.getString("first_name"));
            e.setLastName(rs.getString("last_name"));
            e.setEmail(rs.getString("email"));
            e.setPhone(rs.getString("phone"));
            e.setDepartmentId(rs.getLong("department_id"));
            e.setDepartmentName(rs.getString("department_name"));
            e.setJobTitle(rs.getString("job_title"));
            e.setPayType(PayType.valueOf(rs.getString("pay_type")));
            e.setAnnualSalary(rs.getBigDecimal("annual_salary"));
            e.setHourlyRate(rs.getBigDecimal("hourly_rate"));
            e.setPensionOptIn(rs.getBoolean("pension_opt_in"));
            e.setHireDate(rs.getDate("hire_date"));
            e.setActive(rs.getBoolean("active"));
            return e;
        }
    };

    public EmployeeDao() {
        super();
    }

    public EmployeeDao(DataSource ds) {
        super(ds);
    }

    public Page<Employee> search(EmployeeFilter f) {
        StringBuilder where = new StringBuilder("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<Object>();
        if (f.getQuery() != null && !f.getQuery().trim().isEmpty()) {
            String like = "%" + f.getQuery().trim().toLowerCase() + "%";
            where.append("AND (LOWER(e.first_name) LIKE ? OR LOWER(e.last_name) LIKE ? "
                    + "OR LOWER(e.email) LIKE ? OR LOWER(e.employee_no) LIKE ? "
                    + "OR LOWER(e.first_name || ' ' || e.last_name) LIKE ?) ");
            for (int i = 0; i < 5; i++) {
                params.add(like);
            }
        }
        if (f.getDepartmentId() != null) {
            where.append("AND e.department_id = ? ");
            params.add(f.getDepartmentId());
        }
        if (EmployeeFilter.STATUS_ACTIVE.equals(f.getStatus())) {
            where.append("AND e.active = TRUE ");
        } else if (EmployeeFilter.STATUS_INACTIVE.equals(f.getStatus())) {
            where.append("AND e.active = FALSE ");
        }

        Object[] p = params.toArray();
        int total = queryInt("SELECT COUNT(*) FROM employee e JOIN department d ON d.id = e.department_id "
                + where, p);

        List<Object> pageParams = new ArrayList<Object>(params);
        pageParams.add(f.getPageSize());
        pageParams.add(Page.offset(f.getPage(), f.getPageSize()));
        List<Employee> items = query(SELECT + where + "ORDER BY " + f.orderBy() + " LIMIT ? OFFSET ?",
                MAPPER, pageParams.toArray());
        return new Page<Employee>(items, f.getPage(), f.getPageSize(), total);
    }

    public List<Employee> findActive() {
        return query(SELECT + "WHERE e.active = TRUE ORDER BY e.last_name, e.first_name", MAPPER);
    }

    public List<Employee> findActiveForPeriod(Connection con, java.util.Date periodEnd) throws SQLException {
        return query(con, SELECT + "WHERE e.active = TRUE AND e.hire_date <= ? ORDER BY e.id",
                MAPPER, DateUtil.toSql(periodEnd));
    }

    public List<Employee> findByDepartment(Long departmentId) {
        return query(SELECT + "WHERE e.department_id = ? AND e.active = TRUE ORDER BY e.last_name",
                MAPPER, departmentId);
    }

    public Employee findById(Long id) {
        return queryOne(SELECT + "WHERE e.id = ?", MAPPER, id);
    }

    public Employee findByEmail(String email) {
        return queryOne(SELECT + "WHERE LOWER(e.email) = LOWER(?)", MAPPER, email);
    }

    public int countActive() {
        return queryInt("SELECT COUNT(*) FROM employee WHERE active = TRUE");
    }

    public String nextEmployeeNo() {
        int max = queryInt("SELECT COALESCE(MAX(CAST(SUBSTRING(employee_no, 2) AS INT)), 1000) FROM employee");
        return "E" + (max + 1);
    }

    public Long create(Employee e) {
        if (e.getEmployeeNo() == null) {
            e.setEmployeeNo(nextEmployeeNo());
        }
        Long id = insert("INSERT INTO employee (employee_no, first_name, last_name, email, phone, department_id, "
                + "job_title, pay_type, annual_salary, hourly_rate, pension_opt_in, hire_date, active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                e.getEmployeeNo(), e.getFirstName(), e.getLastName(), e.getEmail().toLowerCase(), e.getPhone(),
                e.getDepartmentId(), e.getJobTitle(), e.getPayType(),
                e.getPayType() == PayType.SALARIED ? e.getAnnualSalary() : null,
                e.getPayType() == PayType.HOURLY ? e.getHourlyRate() : null,
                e.isPensionOptIn(), DateUtil.toSql(e.getHireDate()), e.isActive());
        e.setId(id);
        return id;
    }

    public void save(Employee e) {
        update("UPDATE employee SET first_name = ?, last_name = ?, email = ?, phone = ?, department_id = ?, "
                + "job_title = ?, pay_type = ?, annual_salary = ?, hourly_rate = ?, pension_opt_in = ?, "
                + "hire_date = ?, active = ?, updated_at = ? WHERE id = ?",
                e.getFirstName(), e.getLastName(), e.getEmail().toLowerCase(), e.getPhone(), e.getDepartmentId(),
                e.getJobTitle(), e.getPayType(),
                e.getPayType() == PayType.SALARIED ? e.getAnnualSalary() : null,
                e.getPayType() == PayType.HOURLY ? e.getHourlyRate() : null,
                e.isPensionOptIn(), DateUtil.toSql(e.getHireDate()), e.isActive(), now(), e.getId());
    }

    public void updatePay(Long id, java.math.BigDecimal annualSalary, java.math.BigDecimal hourlyRate) {
        update("UPDATE employee SET annual_salary = COALESCE(?, annual_salary), "
                + "hourly_rate = COALESCE(?, hourly_rate), updated_at = ? WHERE id = ?",
                annualSalary, hourlyRate, now(), id);
    }

    public void setActive(Long id, boolean active) {
        update("UPDATE employee SET active = ?, updated_at = ? WHERE id = ?", active, now(), id);
    }
}
