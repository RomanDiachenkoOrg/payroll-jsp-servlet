package com.acme.payroll.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.TaxBracket;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.util.Money;

/**
 * Monthly payroll calculation rules.
 * <ul>
 * <li>Salaried: gross = annual salary / 12</li>
 * <li>Hourly: rate * regular + rate * 1.5 * overtime, overtime = hours over 40 in a week</li>
 * <li>Income tax: progressive brackets on monthly gross</li>
 * <li>Social contribution and optional pension as a percentage of gross</li>
 * </ul>
 * This class has no database access so it can be unit tested in isolation.
 */
public class PayrollCalculator {

    public static final BigDecimal WEEKLY_HOURS = new BigDecimal("40");
    public static final BigDecimal OVERTIME_FACTOR = new BigDecimal("1.5");
    private static final BigDecimal TWELVE = new BigDecimal("12");

    private final List<TaxBracket> brackets;
    private final BigDecimal socialRate;
    private final BigDecimal pensionRate;

    public PayrollCalculator(List<TaxBracket> brackets, BigDecimal socialRate, BigDecimal pensionRate) {
        List<TaxBracket> sorted = new ArrayList<TaxBracket>(brackets);
        Collections.sort(sorted, new Comparator<TaxBracket>() {
            @Override
            public int compare(TaxBracket a, TaxBracket b) {
                return a.getLowerBound().compareTo(b.getLowerBound());
            }
        });
        this.brackets = sorted;
        this.socialRate = socialRate;
        this.pensionRate = pensionRate;
    }

    /** Regular and overtime hours for a set of weekly timesheets: [regular, overtime]. */
    public BigDecimal[] splitHours(List<Timesheet> timesheets) {
        BigDecimal regular = BigDecimal.ZERO;
        BigDecimal overtime = BigDecimal.ZERO;
        for (Timesheet t : timesheets) {
            BigDecimal week = t.getTotalHours();
            if (week.compareTo(WEEKLY_HOURS) > 0) {
                regular = regular.add(WEEKLY_HOURS);
                overtime = overtime.add(week.subtract(WEEKLY_HOURS));
            } else {
                regular = regular.add(week);
            }
        }
        return new BigDecimal[] {regular, overtime};
    }

    public BigDecimal grossPay(Employee e, BigDecimal regularHours, BigDecimal overtimeHours) {
        if (e.getPayType() == PayType.SALARIED) {
            return Money.round(Money.nz(e.getAnnualSalary()).divide(TWELVE, 10, RoundingMode.HALF_UP));
        }
        BigDecimal rate = Money.nz(e.getHourlyRate());
        BigDecimal regular = rate.multiply(Money.nz(regularHours));
        BigDecimal overtime = rate.multiply(OVERTIME_FACTOR).multiply(Money.nz(overtimeHours));
        return Money.round(regular.add(overtime));
    }

    /** Progressive income tax on a monthly gross amount. */
    public BigDecimal incomeTax(BigDecimal gross) {
        BigDecimal tax = BigDecimal.ZERO;
        for (TaxBracket b : brackets) {
            if (gross.compareTo(b.getLowerBound()) <= 0) {
                break;
            }
            BigDecimal top = b.getUpperBound() == null ? gross : gross.min(b.getUpperBound());
            tax = tax.add(top.subtract(b.getLowerBound()).multiply(b.getRate()));
        }
        return Money.round(tax);
    }

    public Payslip calculate(Employee e, List<Timesheet> approvedTimesheets, BigDecimal otherDeductions) {
        BigDecimal regularHours = BigDecimal.ZERO;
        BigDecimal overtimeHours = BigDecimal.ZERO;
        if (e.getPayType() == PayType.HOURLY) {
            BigDecimal[] split = splitHours(approvedTimesheets);
            regularHours = split[0];
            overtimeHours = split[1];
        }
        BigDecimal gross = grossPay(e, regularHours, overtimeHours);
        BigDecimal tax = incomeTax(gross);
        BigDecimal social = Money.round(gross.multiply(socialRate));
        BigDecimal pension = e.isPensionOptIn() ? Money.round(gross.multiply(pensionRate)) : Money.ZERO;
        BigDecimal other = Money.round(Money.nz(otherDeductions));
        BigDecimal net = Money.round(gross.subtract(tax).subtract(social).subtract(pension).subtract(other));

        Payslip p = new Payslip();
        p.setEmployeeId(e.getId());
        p.setEmployeeName(e.getFullName());
        p.setEmployeeNo(e.getEmployeeNo());
        p.setDepartmentName(e.getDepartmentName());
        p.setRegularHours(regularHours.setScale(2, RoundingMode.HALF_UP));
        p.setOvertimeHours(overtimeHours.setScale(2, RoundingMode.HALF_UP));
        p.setGross(gross);
        p.setIncomeTax(tax);
        p.setSocial(social);
        p.setPension(pension);
        p.setOtherDeductions(other);
        p.setNet(net);
        return p;
    }
}
