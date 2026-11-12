package com.acme.payroll.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Date helpers. The UI uses ISO yyyy-MM-dd everywhere (jQuery UI datepicker
 * is configured with dateFormat 'yy-mm-dd').
 */
public final class DateUtil {

    public static final String ISO = "yyyy-MM-dd";

    private DateUtil() {
    }

    public static Date parse(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat f = new SimpleDateFormat(ISO);
        f.setLenient(false);
        try {
            return f.parse(s.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    public static String format(Date d) {
        return d == null ? "" : new SimpleDateFormat(ISO).format(d);
    }

    public static java.sql.Date toSql(Date d) {
        return d == null ? null : new java.sql.Date(d.getTime());
    }

    public static Date today() {
        return truncate(new Date());
    }

    public static Date truncate(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date addDays(Date d, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DAY_OF_MONTH, days);
        return c.getTime();
    }

    /** Monday of the week containing the given date. */
    public static Date mondayOf(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(truncate(d));
        int dow = c.get(Calendar.DAY_OF_WEEK);
        int delta = (dow == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dow;
        c.add(Calendar.DAY_OF_MONTH, delta);
        return c.getTime();
    }

    public static boolean isMonday(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

    public static Date endOfMonth(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(truncate(d));
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        return c.getTime();
    }

    public static Date startOfMonth(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(truncate(d));
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }
}
