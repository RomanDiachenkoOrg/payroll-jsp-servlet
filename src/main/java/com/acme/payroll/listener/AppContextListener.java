package com.acme.payroll.listener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acme.payroll.util.Database;
import com.acme.payroll.util.JdbcUtil;
import com.acme.payroll.util.ScriptRunner;
import com.acme.payroll.util.SimpleConnectionPool;

/**
 * Creates the connection pool on startup and, on the very first start,
 * creates the schema and loads the demo data.
 */
public class AppContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(AppContextListener.class);

    private SimpleConnectionPool pool;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Properties cfg = Database.loadConfig();
        String url = cfg.getProperty("db.url");
        int size = Integer.parseInt(cfg.getProperty("db.poolSize", "10"));
        log.info("Connecting to {}", url);
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("H2 driver not on classpath", e);
        }
        pool = new SimpleConnectionPool(url, cfg.getProperty("db.user", "sa"), cfg.getProperty("db.password", ""),
                size);
        Database.setDataSource(pool);
        initSchema(pool);
        sce.getServletContext().setAttribute("appVersion", cfg.getProperty("app.version", "1.4"));
    }

    static void initSchema(SimpleConnectionPool ds) {
        Connection con = null;
        ResultSet rs = null;
        try {
            con = ds.getConnection();
            rs = con.getMetaData().getTables(null, null, "APP_USER", null);
            boolean exists = rs.next();
            rs.close();
            rs = null;
            if (exists) {
                log.info("Database schema found");
                return;
            }
            log.info("Empty database - creating schema and loading demo data");
            con.setAutoCommit(false);
            ScriptRunner runner = new ScriptRunner(con);
            runner.run("/db/schema.sql");
            runner.run("/db/seed.sql");
            con.commit();
        } catch (Exception e) {
            JdbcUtil.rollbackQuietly(con);
            throw new IllegalStateException("Database initialisation failed", e);
        } finally {
            JdbcUtil.close(rs, null, con);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (pool != null) {
            pool.shutdown();
        }
    }
}
