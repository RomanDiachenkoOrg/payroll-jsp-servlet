package com.acme.payroll.util;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

/**
 * Minimal fixed-size JDBC connection pool.
 * <p>
 * Connections handed out are proxies; calling {@code close()} returns the
 * physical connection to the pool instead of closing it. We used to run on
 * the container DBCP pool but the embedded H2 setup does not need it.
 */
public class SimpleConnectionPool implements DataSource {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SimpleConnectionPool.class);

    private final String url;
    private final String user;
    private final String password;
    private final int maxSize;
    private final BlockingQueue<Connection> idle;
    private final AtomicInteger created = new AtomicInteger();
    private volatile boolean closed;
    private int loginTimeout = 10;

    public SimpleConnectionPool(String url, String user, String password, int maxSize) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxSize = maxSize;
        this.idle = new ArrayBlockingQueue<Connection>(maxSize);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (closed) {
            throw new SQLException("Connection pool is closed");
        }
        Connection physical = idle.poll();
        if (physical == null && created.get() < maxSize) {
            if (created.incrementAndGet() <= maxSize) {
                physical = DriverManager.getConnection(url, user, password);
                log.debug("Opened physical connection #{}", created.get());
            } else {
                created.decrementAndGet();
            }
        }
        if (physical == null) {
            try {
                physical = idle.poll(loginTimeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted while waiting for a connection", e);
            }
            if (physical == null) {
                throw new SQLException("Timed out waiting for a database connection");
            }
        }
        if (!physical.getAutoCommit()) {
            physical.rollback();
            physical.setAutoCommit(true);
        }
        return wrap(physical);
    }

    @Override
    public Connection getConnection(String username, String pwd) throws SQLException {
        return getConnection();
    }

    private Connection wrap(final Connection physical) {
        InvocationHandler handler = new InvocationHandler() {
            private boolean released;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("close".equals(name)) {
                    if (!released) {
                        released = true;
                        release(physical);
                    }
                    return null;
                }
                if ("isClosed".equals(name)) {
                    return released || physical.isClosed();
                }
                if (released) {
                    throw new SQLException("Connection already returned to pool");
                }
                try {
                    return method.invoke(physical, args);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
        };
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[] {Connection.class}, handler);
    }

    private void release(Connection physical) {
        try {
            if (closed || physical.isClosed()) {
                created.decrementAndGet();
                return;
            }
            if (!physical.getAutoCommit()) {
                physical.rollback();
                physical.setAutoCommit(true);
            }
            if (!idle.offer(physical)) {
                physical.close();
                created.decrementAndGet();
            }
        } catch (SQLException e) {
            log.warn("Discarding broken connection", e);
            created.decrementAndGet();
        }
    }

    public void shutdown() {
        closed = true;
        Connection c;
        while ((c = idle.poll()) != null) {
            try {
                c.close();
            } catch (SQLException ignore) {
                // nothing to do
            }
        }
        log.info("Connection pool shut down");
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        // not supported
    }

    @Override
    public void setLoginTimeout(int seconds) {
        this.loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() {
        return loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Not a wrapper");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }
}
