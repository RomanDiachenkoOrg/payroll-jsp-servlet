package com.acme.payroll.util;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class JdbcUtil {

    private JdbcUtil() {
    }

    public static void close(ResultSet rs, Statement st, Connection con) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
    }

    public static void rollbackQuietly(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignore) {
                // ignore
            }
        }
    }

    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
    }
}
