package com.acme.payroll.web.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.payroll.dao.EmployeeDao;
import com.acme.payroll.dao.PayRunDao;
import com.acme.payroll.dao.PayslipDao;
import com.acme.payroll.dao.ReportDao;
import com.acme.payroll.dao.TimesheetDao;
import com.acme.payroll.model.DepartmentCost;
import com.acme.payroll.model.PayRun;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.Role;
import com.acme.payroll.model.TimesheetStatus;
import com.acme.payroll.model.User;
import com.acme.payroll.util.DateUtil;

public class DashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        PayRunDao payRunDao = new PayRunDao();

        PayRun next = payRunDao.findNextOpen();
        req.setAttribute("nextPayDate", next != null ? next.getPayDate() : DateUtil.endOfMonth(DateUtil.today()));

        if (user.getRole() == Role.EMPLOYEE) {
            List<Payslip> slips = new PayslipDao().findByEmployee(user.getEmployeeId());
            req.setAttribute("payslips", slips.size() > 3 ? slips.subList(0, 3) : slips);
            req.setAttribute("myTimesheets", new TimesheetDao().find(null, user.getEmployeeId(), null));
            render("dashboard", req, resp);
            return;
        }

        req.setAttribute("headcount", new EmployeeDao().countActive());
        req.setAttribute("pendingTimesheets", new TimesheetDao().countByStatus(TimesheetStatus.SUBMITTED));
        PayRun last = payRunDao.findLastPaid();
        req.setAttribute("lastRun", last);
        if (last != null) {
            List<DepartmentCost> costs = new ReportDao().costByDepartment(last.getPayDate(), last.getPayDate());
            BigDecimal max = BigDecimal.ONE;
            for (DepartmentCost c : costs) {
                max = max.max(c.getGross());
            }
            req.setAttribute("deptCosts", costs);
            req.setAttribute("deptMax", max);
        }
        req.setAttribute("recentRuns", payRunDao.findRecentPaid(6));
        render("dashboard", req, resp);
    }
}
