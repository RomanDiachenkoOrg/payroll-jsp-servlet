package com.acme.payroll.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.acme.payroll.model.TaxBracket;
import com.acme.payroll.util.JdbcUtil;

/** Tax brackets and simple key/value application settings. */
public class SettingsDao extends BaseDao {

    public static final String SOCIAL_RATE = "social.rate";
    public static final String PENSION_RATE = "pension.rate";

    private static final RowMapper<TaxBracket> BRACKET_MAPPER = new RowMapper<TaxBracket>() {
        @Override
        public TaxBracket map(ResultSet rs) throws SQLException {
            TaxBracket b = new TaxBracket();
            b.setId(rs.getLong("id"));
            b.setLowerBound(rs.getBigDecimal("lower_bound"));
            b.setUpperBound(rs.getBigDecimal("upper_bound"));
            b.setRate(rs.getBigDecimal("rate"));
            return b;
        }
    };

    public SettingsDao() {
        super();
    }

    public SettingsDao(DataSource ds) {
        super(ds);
    }

    public List<TaxBracket> findBrackets() {
        return query("SELECT * FROM tax_bracket ORDER BY lower_bound", BRACKET_MAPPER);
    }

    public void replaceBrackets(List<TaxBracket> brackets) {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            update(con, "DELETE FROM tax_bracket");
            for (TaxBracket b : brackets) {
                insert(con, "INSERT INTO tax_bracket (lower_bound, upper_bound, rate) VALUES (?, ?, ?)",
                        b.getLowerBound(), b.getUpperBound(), b.getRate());
            }
            con.commit();
        } catch (SQLException e) {
            JdbcUtil.rollbackQuietly(con);
            throw new DaoException("Could not save tax brackets", e);
        } finally {
            JdbcUtil.close(null, null, con);
        }
    }

    public String get(String key, String defaultValue) {
        String v = queryOne("SELECT setting_value FROM app_setting WHERE setting_key = ?", new RowMapper<String>() {
            @Override
            public String map(ResultSet rs) throws SQLException {
                return rs.getString(1);
            }
        }, key);
        return v == null ? defaultValue : v;
    }

    public BigDecimal getDecimal(String key, String defaultValue) {
        return new BigDecimal(get(key, defaultValue));
    }

    public void set(String key, String value) {
        if (update("UPDATE app_setting SET setting_value = ? WHERE setting_key = ?", value, key) == 0) {
            update("INSERT INTO app_setting (setting_key, setting_value) VALUES (?, ?)", key, value);
        }
    }
}
