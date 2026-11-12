package com.acme.payroll.service;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.TaxBracket;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.model.TimesheetEntry;

public class PayrollCalculatorTest {

    private PayrollCalculator calc;

    @Before
    public void setUp() {
        List<TaxBracket> brackets = Arrays.asList(
                bracket("4000", null, "0.25"),
                bracket("0", "1000", "0"),
                bracket("1000", "4000", "0.15"));
        calc = new PayrollCalculator(brackets, new BigDecimal("0.05"), new BigDecimal("0.03"));
    }

    @Test
    public void noTaxUpToFirstBracket() {
        assertMoney("0.00", calc.incomeTax(new BigDecimal("950.00")));
        assertMoney("0.00", calc.incomeTax(new BigDecimal("1000.00")));
    }

    @Test
    public void progressiveTaxAcrossBrackets() {
        // 3000 * 15% = 450
        assertMoney("450.00", calc.incomeTax(new BigDecimal("4000.00")));
        // 450 + 6000 * 25% = 1950
        assertMoney("1950.00", calc.incomeTax(new BigDecimal("10000.00")));
        // 0.01 above the first bracket
        assertMoney("0.00", calc.incomeTax(new BigDecimal("1000.01")));
        assertMoney("0.15", calc.incomeTax(new BigDecimal("1001.00")));
    }

    @Test
    public void salariedPayslip() {
        Employee e = salaried("132000", true);
        Payslip p = calc.calculate(e, Collections.<Timesheet>emptyList(), new BigDecimal("120"));
        assertMoney("11000.00", p.getGross());
        assertMoney("2200.00", p.getIncomeTax());
        assertMoney("550.00", p.getSocial());
        assertMoney("330.00", p.getPension());
        assertMoney("120.00", p.getOtherDeductions());
        assertMoney("7800.00", p.getNet());
    }

    @Test
    public void salaryIsRoundedHalfUp() {
        Employee e = salaried("118000", false);
        Payslip p = calc.calculate(e, Collections.<Timesheet>emptyList(), BigDecimal.ZERO);
        assertMoney("9833.33", p.getGross());
        assertMoney("0.00", p.getPension());
    }

    @Test
    public void hourlyWithoutOvertime() {
        Employee e = hourly("20.00");
        Payslip p = calc.calculate(e, Arrays.asList(week(8, 8, 8, 8, 8, 0, 0), week(8, 8, 8, 8, 6, 0, 0)),
                BigDecimal.ZERO);
        assertMoney("78.00", p.getRegularHours());
        assertMoney("0.00", p.getOvertimeHours());
        assertMoney("1560.00", p.getGross());
    }

    @Test
    public void hourlyOvertimeIsPerWeek() {
        Employee e = hourly("20.00");
        // 46 hours -> 40 regular + 6 overtime; second week 30 hours regular
        List<Timesheet> sheets = Arrays.asList(week(10, 10, 9, 9, 8, 0, 0), week(6, 6, 6, 6, 6, 0, 0));
        Payslip p = calc.calculate(e, sheets, BigDecimal.ZERO);
        assertMoney("70.00", p.getRegularHours());
        assertMoney("6.00", p.getOvertimeHours());
        // 70 * 20 + 6 * 30 = 1580
        assertMoney("1580.00", p.getGross());
    }

    @Test
    public void netNeverIgnoresOtherDeductions() {
        Employee e = salaried("60000", false);
        Payslip p = calc.calculate(e, Collections.<Timesheet>emptyList(), new BigDecimal("85"));
        // gross 5000, tax 450 + 250 = 700, social 250, other 85
        assertMoney("3965.00", p.getNet());
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(new BigDecimal(expected), actual);
    }

    private static TaxBracket bracket(String lower, String upper, String rate) {
        TaxBracket b = new TaxBracket();
        b.setLowerBound(new BigDecimal(lower));
        b.setUpperBound(upper == null ? null : new BigDecimal(upper));
        b.setRate(new BigDecimal(rate));
        return b;
    }

    private static Employee salaried(String annual, boolean pension) {
        Employee e = new Employee();
        e.setId(1L);
        e.setFirstName("Test");
        e.setLastName("Salaried");
        e.setPayType(PayType.SALARIED);
        e.setAnnualSalary(new BigDecimal(annual));
        e.setPensionOptIn(pension);
        return e;
    }

    private static Employee hourly(String rate) {
        Employee e = new Employee();
        e.setId(2L);
        e.setFirstName("Test");
        e.setLastName("Hourly");
        e.setPayType(PayType.HOURLY);
        e.setHourlyRate(new BigDecimal(rate));
        return e;
    }

    private static Timesheet week(int... hours) {
        Timesheet t = new Timesheet();
        List<TimesheetEntry> entries = new ArrayList<TimesheetEntry>();
        for (int h : hours) {
            TimesheetEntry e = new TimesheetEntry();
            e.setHours(new BigDecimal(h));
            entries.add(e);
        }
        t.setEntries(entries);
        return t;
    }
}
