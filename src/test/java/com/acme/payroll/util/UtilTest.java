package com.acme.payroll.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

public class UtilTest {

    @Test
    public void passwordHashRoundTrip() {
        String salt = PasswordUtil.newSalt();
        String hash = PasswordUtil.hash("Passw0rd!", salt);
        assertEquals(64, hash.length());
        assertTrue(PasswordUtil.matches("Passw0rd!", salt, hash));
        assertFalse(PasswordUtil.matches("passw0rd!", salt, hash));
    }

    @Test
    public void moneyRoundsHalfUp() {
        assertEquals(new BigDecimal("2.35"), Money.round(new BigDecimal("2.345")));
        assertEquals(new BigDecimal("2.34"), Money.round(new BigDecimal("2.3449")));
        assertEquals(Money.ZERO, Money.round(null));
        assertNull(Money.parse("abc"));
        assertEquals(new BigDecimal("1200.50"), Money.parse(" 1,200.50 "));
    }

    @Test
    public void dateParsingIsStrict() {
        assertNull(DateUtil.parse("2026-02-30"));
        assertNull(DateUtil.parse("not a date"));
        assertEquals("2026-11-09", DateUtil.format(DateUtil.mondayOf(DateUtil.parse("2026-11-12"))));
        assertEquals("2026-11-09", DateUtil.format(DateUtil.mondayOf(DateUtil.parse("2026-11-15"))));
        assertTrue(DateUtil.isMonday(DateUtil.parse("2026-11-02")));
    }

    @Test
    public void csvEscaping() {
        assertEquals("plain", CsvWriter.escape("plain"));
        assertEquals("\"a,b\"", CsvWriter.escape("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", CsvWriter.escape("say \"hi\""));
        assertEquals("1000.00", CsvWriter.escape(new BigDecimal("1000.00")));
    }

    @Test
    public void pageOffsets() {
        assertEquals(0, Page.offset(1, 10));
        assertEquals(20, Page.offset(3, 10));
        assertEquals(0, Page.offset(0, 10));
    }
}
