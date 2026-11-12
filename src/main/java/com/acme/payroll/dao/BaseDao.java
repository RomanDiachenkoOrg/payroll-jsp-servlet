package com.acme.payroll.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import com.acme.payroll.util.Database;
import com.acme.payroll.util.JdbcUtil;

/**
 * Common plumbing for the JDBC DAOs. Each DAO method opens and closes its own
 * connection unless a connection is passed in explicitly (used by services that
 * need a transaction across several DAOs).
 */
public abstract class BaseDao {

    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private final DataSource dataSource;

    protected BaseDao() {
        this(Database.getDataSource());
    }

    protected BaseDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected DataSource getDataSource() {
        return dataSource;
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    protected <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        Connection con = null;
        try {
            con = getConnection();
            return query(con, sql, mapper, params);
        } catch (SQLException e) {
            throw new DaoException("Query failed: " + sql, e);
        } finally {
            JdbcUtil.close(null, null, con);
        }
    }

    protected <T> List<T> query(Connection con, String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sql);
            bind(ps, params);
            rs = ps.executeQuery();
            List<T> result = new ArrayList<T>();
            while (rs.next()) {
                result.add(mapper.map(rs));
            }
            return result;
        } finally {
            JdbcUtil.close(rs, ps, null);
        }
    }

    protected <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> list = query(sql, mapper, params);
        return list.isEmpty() ? null : list.get(0);
    }

    protected int queryInt(String sql, Object... params) {
        Number n = queryOne(sql, new RowMapper<Number>() {
            @Override
            public Number map(ResultSet rs) throws SQLException {
                Object o = rs.getObject(1);
                return o == null ? 0 : (Number) o;
            }
        }, params);
        return n == null ? 0 : n.intValue();
    }

    protected BigDecimal queryDecimal(String sql, Object... params) {
        BigDecimal d = queryOne(sql, new RowMapper<BigDecimal>() {
            @Override
            public BigDecimal map(ResultSet rs) throws SQLException {
                return rs.getBigDecimal(1);
            }
        }, params);
        return d == null ? BigDecimal.ZERO : d;
    }

    protected int update(String sql, Object... params) {
        Connection con = null;
        try {
            con = getConnection();
            return update(con, sql, params);
        } catch (SQLException e) {
            throw new DaoException("Update failed: " + sql, e);
        } finally {
            JdbcUtil.close(null, null, con);
        }
    }

    protected int update(Connection con, String sql, Object... params) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            bind(ps, params);
            return ps.executeUpdate();
        } finally {
            JdbcUtil.close(null, ps, null);
        }
    }

    protected Long insert(String sql, Object... params) {
        Connection con = null;
        try {
            con = getConnection();
            return insert(con, sql, params);
        } catch (SQLException e) {
            throw new DaoException("Insert failed: " + sql, e);
        } finally {
            JdbcUtil.close(null, null, con);
        }
    }

    protected Long insert(Connection con, String sql, Object... params) throws SQLException {
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            bind(ps, params);
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getLong(1) : null;
        } finally {
            JdbcUtil.close(keys, ps, null);
        }
    }

    private void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            int idx = i + 1;
            if (p == null) {
                ps.setNull(idx, Types.NULL);
            } else if (p instanceof Timestamp) {
                ps.setTimestamp(idx, (Timestamp) p);
            } else if (p instanceof java.sql.Date) {
                ps.setDate(idx, (java.sql.Date) p);
            } else if (p instanceof Date) {
                ps.setDate(idx, new java.sql.Date(((Date) p).getTime()));
            } else if (p instanceof Enum) {
                ps.setString(idx, ((Enum<?>) p).name());
            } else {
                ps.setObject(idx, p);
            }
        }
    }

    protected static Long getLong(ResultSet rs, String col) throws SQLException {
        long v = rs.getLong(col);
        return rs.wasNull() ? null : v;
    }

    protected static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}
