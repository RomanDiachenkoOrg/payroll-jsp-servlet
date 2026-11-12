package com.acme.payroll.util;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;

/** Tiny RFC 4180 CSV writer. */
public class CsvWriter {

    private final Writer out;

    public CsvWriter(Writer out) {
        this.out = out;
    }

    public CsvWriter row(Object... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                out.write(',');
            }
            out.write(escape(values[i]));
        }
        out.write("\r\n");
        return this;
    }

    public void flush() throws IOException {
        out.flush();
    }

    static String escape(Object value) {
        if (value == null) {
            return "";
        }
        String s = value instanceof BigDecimal ? ((BigDecimal) value).toPlainString() : value.toString();
        if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}
