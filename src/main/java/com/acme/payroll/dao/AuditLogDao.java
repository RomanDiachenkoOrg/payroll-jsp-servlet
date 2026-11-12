package com.acme.payroll.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.acme.payroll.model.AuditEntry;

public class AuditLogDao extends BaseDao {

    private static final RowMapper<AuditEntry> MAPPER = new RowMapper<AuditEntry>() {
        @Override
        public AuditEntry map(ResultSet rs) throws SQLException {
            AuditEntry a = new AuditEntry();
            a.setId(rs.getLong("id"));
            a.setCreatedAt(rs.getTimestamp("created_at"));
            a.setUserEmail(rs.getString("user_email"));
            a.setAction(rs.getString("action"));
            a.setEntity(rs.getString("entity"));
            a.setEntityId(getLong(rs, "entity_id"));
            a.setDetails(rs.getString("details"));
            return a;
        }
    };

    public AuditLogDao() {
        super();
    }

    public AuditLogDao(DataSource ds) {
        super(ds);
    }

    public void log(String user, String action, String entity, Long entityId, String details) {
        insert("INSERT INTO audit_log (created_at, user_email, action, entity, entity_id, details) "
                + "VALUES (?, ?, ?, ?, ?, ?)", now(), user, action, entity, entityId,
                details != null && details.length() > 500 ? details.substring(0, 500) : details);
    }

    public List<AuditEntry> find(String user, String action, String entity, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_log WHERE 1 = 1 ");
        List<Object> params = new ArrayList<Object>();
        if (user != null && !user.isEmpty()) {
            sql.append("AND LOWER(user_email) LIKE ? ");
            params.add("%" + user.toLowerCase() + "%");
        }
        if (action != null && !action.isEmpty()) {
            sql.append("AND action = ? ");
            params.add(action);
        }
        if (entity != null && !entity.isEmpty()) {
            sql.append("AND entity = ? ");
            params.add(entity);
        }
        sql.append("ORDER BY created_at DESC, id DESC LIMIT ?");
        params.add(limit);
        return query(sql.toString(), MAPPER, params.toArray());
    }

    public List<String> findActions() {
        return query("SELECT DISTINCT action FROM audit_log ORDER BY action", new RowMapper<String>() {
            @Override
            public String map(ResultSet rs) throws SQLException {
                return rs.getString(1);
            }
        });
    }
}
