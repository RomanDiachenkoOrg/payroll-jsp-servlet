package com.acme.payroll.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.acme.payroll.model.DeductionType;

public class DeductionDao extends BaseDao {

    private static final RowMapper<DeductionType> MAPPER = new RowMapper<DeductionType>() {
        @Override
        public DeductionType map(ResultSet rs) throws SQLException {
            DeductionType t = new DeductionType();
            t.setId(rs.getLong("id"));
            t.setCode(rs.getString("code"));
            t.setName(rs.getString("name"));
            t.setDescription(rs.getString("description"));
            t.setActive(rs.getBoolean("active"));
            return t;
        }
    };

    public DeductionDao() {
        super();
    }

    public DeductionDao(DataSource ds) {
        super(ds);
    }

    public List<DeductionType> findTypes() {
        return query("SELECT * FROM deduction_type ORDER BY name", MAPPER);
    }

    /** Sum of all active recurring deductions of the employee (per month). */
    public BigDecimal totalForEmployee(Connection con, Long employeeId) throws SQLException {
        List<BigDecimal> list = query(con, "SELECT COALESCE(SUM(ed.amount), 0) FROM employee_deduction ed "
                + "JOIN deduction_type t ON t.id = ed.deduction_type_id "
                + "WHERE ed.employee_id = ? AND t.active = TRUE",
                new RowMapper<BigDecimal>() {
                    @Override
                    public BigDecimal map(ResultSet rs) throws SQLException {
                        return rs.getBigDecimal(1);
                    }
                }, employeeId);
        return list.isEmpty() || list.get(0) == null ? BigDecimal.ZERO : list.get(0);
    }

    /** Deduction name to amount, for the employee profile page. */
    public Map<String, BigDecimal> findForEmployee(Long employeeId) {
        final Map<String, BigDecimal> map = new LinkedHashMap<String, BigDecimal>();
        query("SELECT t.name, ed.amount FROM employee_deduction ed "
                + "JOIN deduction_type t ON t.id = ed.deduction_type_id WHERE ed.employee_id = ? ORDER BY t.name",
                new RowMapper<Void>() {
                    @Override
                    public Void map(ResultSet rs) throws SQLException {
                        map.put(rs.getString(1), rs.getBigDecimal(2));
                        return null;
                    }
                }, employeeId);
        return map;
    }
}
