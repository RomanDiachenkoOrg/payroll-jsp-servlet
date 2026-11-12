package com.acme.payroll.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.payroll.dao.EmployeeDao;
import com.acme.payroll.dao.PayslipDao;
import com.acme.payroll.dao.SettingsDao;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.Role;
import com.acme.payroll.model.User;
import com.acme.payroll.util.CsvWriter;
import com.acme.payroll.util.DateUtil;

/**
 * /payslips/view?id=   payslip with breakdown
 * /payslips/print?id=  print friendly page
 * /payslips/csv?id=    CSV download
 */
public class PayslipServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Payslip p = new PayslipDao().findById(requiredId(req));
        if (p == null) {
            throw new NotFoundException();
        }
        User u = currentUser(req);
        if (u.getRole() == Role.EMPLOYEE && !p.getEmployeeId().equals(u.getEmployeeId())) {
            throw new ForbiddenException();
        }
        if (u.getRole() == Role.MANAGER && !p.getEmployeeId().equals(u.getEmployeeId())
                && !new EmployeeDao().findById(p.getEmployeeId()).getDepartmentId().equals(u.getDepartmentId())) {
            throw new ForbiddenException();
        }
        req.setAttribute("payslip", p);
        req.setAttribute("employee", new EmployeeDao().findById(p.getEmployeeId()));
        req.setAttribute("companyName", new SettingsDao().get("company.name", "Acme Corporation"));
        if ("/view".equals(action)) {
            render("payslips/view", req, resp);
        } else if ("/print".equals(action)) {
            render("payslips/print", req, resp);
        } else if ("/csv".equals(action)) {
            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=\"payslip-" + p.getEmployeeNo() + "-"
                    + DateUtil.format(p.getPeriodStart()) + ".csv\"");
            CsvWriter csv = new CsvWriter(resp.getWriter());
            csv.row("Item", "Amount (USD)");
            csv.row("Employee", p.getEmployeeNo() + " " + p.getEmployeeName());
            csv.row("Period", DateUtil.format(p.getPeriodStart()) + " - " + DateUtil.format(p.getPeriodEnd()));
            csv.row("Pay date", DateUtil.format(p.getPayDate()));
            csv.row("Regular hours", p.getRegularHours());
            csv.row("Overtime hours", p.getOvertimeHours());
            csv.row("Gross pay", p.getGross());
            csv.row("Income tax", p.getIncomeTax());
            csv.row("Social contribution", p.getSocial());
            csv.row("Pension", p.getPension());
            csv.row("Other deductions", p.getOtherDeductions());
            csv.row("Net pay", p.getNet());
            csv.flush();
        } else {
            throw new NotFoundException();
        }
    }
}
