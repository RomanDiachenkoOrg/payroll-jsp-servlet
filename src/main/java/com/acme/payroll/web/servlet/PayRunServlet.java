package com.acme.payroll.web.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.payroll.dao.PayRunDao;
import com.acme.payroll.dao.PayslipDao;
import com.acme.payroll.model.PayRun;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.service.BusinessException;
import com.acme.payroll.service.PayRunService;
import com.acme.payroll.util.CsvWriter;
import com.acme.payroll.util.DateUtil;

public class PayRunServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private transient PayRunDao payRunDao;
    private transient PayslipDao payslipDao;
    private transient PayRunService payRunService;

    @Override
    public void init() throws ServletException {
        payRunDao = new PayRunDao();
        payslipDao = new PayslipDao();
        payRunService = new PayRunService();
    }

    @Override
    protected void handleGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/".equals(action)) {
            req.setAttribute("payRuns", payRunDao.findAll());
            Date start = DateUtil.startOfMonth(DateUtil.addDays(DateUtil.endOfMonth(DateUtil.today()), 1));
            req.setAttribute("suggestStart", DateUtil.format(start));
            req.setAttribute("suggestEnd", DateUtil.format(DateUtil.endOfMonth(start)));
            render("payruns/list", req, resp);
        } else if ("/view".equals(action)) {
            PayRun run = load(req);
            req.setAttribute("payRun", run);
            req.setAttribute("payslips", payslipDao.findByPayRun(run.getId()));
            render("payruns/view", req, resp);
        } else if ("/export".equals(action)) {
            export(load(req), resp);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    protected void handlePost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String user = currentUser(req).getEmail();
        if ("/create".equals(action)) {
            try {
                PayRun run = payRunService.create(date(req, "periodStart"), date(req, "periodEnd"),
                        date(req, "payDate"), user);
                flash(req, "Pay run created.");
                redirect("/payruns/view?id=" + run.getId(), req, resp);
            } catch (BusinessException e) {
                flashError(req, e);
                redirect("/payruns", req, resp);
            }
            return;
        }
        Long id = requiredId(req);
        try {
            if ("/calculate".equals(action)) {
                int n = payRunService.calculate(id, user);
                flash(req, "Calculated " + n + " payslips.");
            } else if ("/approve".equals(action)) {
                payRunService.approve(id, user);
                flash(req, "Pay run approved.");
            } else if ("/pay".equals(action)) {
                payRunService.markPaid(id, user);
                flash(req, "Pay run marked as paid. It is now locked.");
            } else if ("/delete".equals(action)) {
                payRunService.delete(id, user);
                flash(req, "Pay run deleted.");
                redirect("/payruns", req, resp);
                return;
            } else {
                throw new NotFoundException();
            }
        } catch (BusinessException e) {
            flashError(req, e);
        }
        redirect("/payruns/view?id=" + id, req, resp);
    }

    private PayRun load(HttpServletRequest req) {
        PayRun run = payRunDao.findById(requiredId(req));
        if (run == null) {
            throw new NotFoundException();
        }
        return run;
    }

    private void export(PayRun run, HttpServletResponse resp) throws IOException {
        List<Payslip> slips = payslipDao.findByPayRun(run.getId());
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"payrun-"
                + DateUtil.format(run.getPeriodStart()) + ".csv\"");
        CsvWriter csv = new CsvWriter(resp.getWriter());
        csv.row("Employee No", "Employee", "Department", "Regular hours", "Overtime hours", "Gross",
                "Income tax", "Social", "Pension", "Net");
        for (Payslip p : slips) {
            csv.row(p.getEmployeeNo(), p.getEmployeeName(), p.getDepartmentName(), p.getRegularHours(),
                    p.getOvertimeHours(), p.getGross(), p.getIncomeTax(), p.getSocial(), p.getPension(), p.getNet());
        }
        csv.flush();
    }
}
