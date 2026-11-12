package com.acme.payroll.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a SQL script from the classpath. Statements are separated by ';'
 * at the end of a line; lines starting with "--" are comments.
 */
public class ScriptRunner {

    private static final Logger log = LoggerFactory.getLogger(ScriptRunner.class);

    private final Connection connection;

    public ScriptRunner(Connection connection) {
        this.connection = connection;
    }

    public int run(String resource) throws IOException, SQLException {
        InputStream in = ScriptRunner.class.getResourceAsStream(resource);
        if (in == null) {
            throw new IOException("Script not found: " + resource);
        }
        int count = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        Statement st = connection.createStatement();
        try {
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                sql.append(line).append('\n');
                if (trimmed.endsWith(";")) {
                    String stmt = sql.toString().trim();
                    st.execute(stmt.substring(0, stmt.length() - 1));
                    sql.setLength(0);
                    count++;
                }
            }
        } finally {
            st.close();
            reader.close();
        }
        log.info("Executed {} statements from {}", count, resource);
        return count;
    }
}
