package com.acme.payroll.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * Static holder for the application DataSource (initialised by
 * {@link com.acme.payroll.listener.AppContextListener}).
 */
public final class Database {

    private static DataSource dataSource;

    private Database() {
    }

    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("Database has not been initialised");
        }
        return dataSource;
    }

    public static synchronized void setDataSource(DataSource ds) {
        dataSource = ds;
    }

    /** Loads db.properties from the classpath; system properties override file values. */
    public static Properties loadConfig() {
        Properties props = new Properties();
        InputStream in = Database.class.getResourceAsStream("/db.properties");
        try {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read db.properties", e);
        } finally {
            JdbcUtil.closeQuietly(in);
        }
        for (String key : new String[] {"db.url", "db.user", "db.password", "db.poolSize"}) {
            String override = System.getProperty("payroll." + key);
            if (override != null) {
                props.setProperty(key, override);
            }
        }
        return props;
    }
}
