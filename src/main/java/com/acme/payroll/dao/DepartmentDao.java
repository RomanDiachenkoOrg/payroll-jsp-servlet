package com.acme.payroll.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.acme.payroll.model.Department;

public class DepartmentDao extends BaseDao {

    private static final String SELECT =
            "SELECT d.id, d.code, d.name, d.cost_center, "
            + " (SELECT COUNT(*) FROM employee e WHERE e.department_id = d.id AND e.active = TRUE) AS active_count, "
            + " (SELECT COUNT(*) FROM employee e WHERE e.department_id = d.id) AS total_count "
            + "FROM department d ";

    private static final RowMapper<Department> MAPPER = new RowMapper<Department>() {
        @Override
        public Department map(ResultSet rs) throws SQLException {
            Department d = new Department();
            d.setId(rs.getLong("id"));
            d.setCode(rs.getString("code"));
            d.setName(rs.getString("name"));
            d.setCostCenter(rs.getString("cost_center"));
            d.setActiveEmployees(rs.getInt("active_count"));
            d.setTotalEmployees(rs.getInt("total_count"));
            return d;
        }
    };

    public DepartmentDao() {
        super();
    }

    public DepartmentDao(DataSource ds) {
        super(ds);
    }

    public List<Department> findAll() {
        return query(SELECT + "ORDER BY d.name", MAPPER);
    }

    public Department findById(Long id) {
        return queryOne(SELECT + "WHERE d.id = ?", MAPPER, id);
    }

    public Department findByName(String name) {
        return queryOne(SELECT + "WHERE UPPER(d.name) = UPPER(?)", MAPPER, name);
    }

    public Department findByCode(String code) {
        return queryOne(SELECT + "WHERE UPPER(d.code) = UPPER(?)", MAPPER, code);
    }

    public Long create(Department d) {
        Long id = insert("INSERT INTO department (code, name, cost_center) VALUES (?, ?, ?)",
                d.getCode().toUpperCase(), d.getName(), d.getCostCenter());
        d.setId(id);
        return id;
    }

    public void save(Department d) {
        update("UPDATE department SET code = ?, name = ?, cost_center = ? WHERE id = ?",
                d.getCode().toUpperCase(), d.getName(), d.getCostCenter(), d.getId());
    }

    public int countActiveEmployees(Long departmentId) {
        return queryInt("SELECT COUNT(*) FROM employee WHERE department_id = ? AND active = TRUE", departmentId);
    }

    public int countAllEmployees(Long departmentId) {
        return queryInt("SELECT COUNT(*) FROM employee WHERE department_id = ?", departmentId);
    }

    public void delete(Long id) {
        update("DELETE FROM department WHERE id = ?", id);
    }
}
