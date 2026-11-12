package com.acme.payroll.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.acme.payroll.model.Role;
import com.acme.payroll.model.User;

public class UserDao extends BaseDao {

    private static final RowMapper<User> MAPPER = new RowMapper<User>() {
        @Override
        public User map(ResultSet rs) throws SQLException {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setEmail(rs.getString("email"));
            u.setFullName(rs.getString("full_name"));
            u.setPasswordHash(rs.getString("password_hash"));
            u.setSalt(rs.getString("salt"));
            u.setRole(Role.valueOf(rs.getString("role")));
            u.setDepartmentId(getLong(rs, "department_id"));
            u.setEmployeeId(getLong(rs, "employee_id"));
            u.setActive(rs.getBoolean("active"));
            u.setLastLogin(rs.getTimestamp("last_login"));
            return u;
        }
    };

    public UserDao() {
        super();
    }

    public UserDao(DataSource ds) {
        super(ds);
    }

    public User findByEmail(String email) {
        return queryOne("SELECT * FROM app_user WHERE LOWER(email) = LOWER(?)", MAPPER, email);
    }

    public List<User> findAll() {
        return query("SELECT * FROM app_user ORDER BY role, email", MAPPER);
    }

    public void touchLastLogin(Long id) {
        update("UPDATE app_user SET last_login = ? WHERE id = ?", now(), id);
    }

    public void setActive(Long id, boolean active) {
        update("UPDATE app_user SET active = ? WHERE id = ?", active, id);
    }
}
