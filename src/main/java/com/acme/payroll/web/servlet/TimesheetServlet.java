package com.acme.payroll.web.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.payroll.dao.EmployeeDao;
import com.acme.payroll.dao.TimesheetDao;
import com.acme.payroll.model.Role;
import com.acme.payroll.model.Timesheet;
import com.acme.payroll.model.TimesheetEntry;
import com.acme.payroll.model.TimesheetStatus;
import com.acme.payroll.model.User;
import com.acme.payroll.service.BusinessException;
import com.acme.payroll.service.TimesheetService;
import com.acme.payroll.service.ValidationErrors;
import com.acme.payroll.util.DateUtil;
import com.acme.payroll.util.Money;

public class TimesheetServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private transient TimesheetDao timesheetDao;
    private transient TimesheetService timesheetService;

    @Override
    public void init() throws ServletException {
        timesheetDao = new TimesheetDao();
        timesheetService = new TimesheetService();
    }

    @Override
    protected void handleGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if ("/".equals(action)) {
            String s = str(req, "status");
            TimesheetStatus status = null;
            if (s != null && !s.isEmpty()) {
                try {
                    status = TimesheetStatus.valueOf(s);
                } catch (IllegalArgumentException e) {
                    status = null;
                }
            }
            req.setAttribute("status", status);
            req.setAttribute("statuses", TimesheetStatus.values());
            req.setAttribute("timesheets", timesheetService.listFor(user, status));
            render("timesheets/list", req, resp);
        } else if ("/new".equals(action)) {
            if (user.getRole() == Role.MANAGER && user.getEmployeeId() == null) {
                throw new ForbiddenException();
            }
            Long employeeId = user.getRole() == Role.ADMIN ? longParam(req, "employeeId") : user.getEmployeeId();
            Date week = DateUtil.mondayOf(DateUtil.today());
            showForm(TimesheetService.blank(employeeId, week), null, req, resp);
        } else if ("/edit".equals(action)) {
            Timesheet t = load(req);
            if (!timesheetService.canEdit(user, t)) {
                flashError(req, "This timesheet can no longer be edited.");
                redirect("/timesheets/view?id=" + t.getId(), req, resp);
                return;
            }
            showForm(t, null, req, resp);
        } else if ("/view".equals(action)) {
            Timesheet t = load(req);
            if (!timesheetService.canView(user, t)) {
                throw new ForbiddenException();
            }
            req.setAttribute("timesheet", t);
            req.setAttribute("canEdit", timesheetService.canEdit(user, t));
            req.setAttribute("canDecide", timesheetService.canDecide(user, t));
            render("timesheets/view", req, resp);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    protected void handlePost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if ("/save".equals(action)) {
            Timesheet t = bind(req, user);
            boolean submit = "submit".equals(req.getParameter("op"));
            ValidationErrors errors = timesheetService.save(t, submit, user);
            if (!errors.isEmpty()) {
                showForm(t, errors, req, resp);
                return;
            }
            flash(req, submit ? "Timesheet submitted for approval." : "Timesheet saved as draft.");
            redirect("/timesheets/view?id=" + t.getId(), req, resp);
            return;
        }
        Long id = requiredId(req);
        try {
            if ("/submit".equals(action)) {
                timesheetService.submit(id, user);
                flash(req, "Timesheet submitted for approval.");
            } else if ("/approve".equals(action)) {
                timesheetService.approve(id, user);
                flash(req, "Timesheet approved.");
            } else if ("/reject".equals(action)) {
                timesheetService.reject(id, str(req, "reason"), user);
                flash(req, "Timesheet rejected.");
            } else {
                throw new NotFoundException();
            }
        } catch (BusinessException e) {
            flashError(req, e);
        }
        redirect("/timesheets/view?id=" + id, req, resp);
    }

    private Timesheet load(HttpServletRequest req) {
        Timesheet t = timesheetDao.findById(requiredId(req));
        if (t == null) {
            throw new NotFoundException();
        }
        return t;
    }

    private Timesheet bind(HttpServletRequest req, User user) {
        Long id = longParam(req, "id");
        Timesheet t;
        if (id != null) {
            t = load(req);
            if (!timesheetService.canEdit(user, t)) {
                throw new ForbiddenException();
            }
        } else {
            Long employeeId = user.getRole() == Role.ADMIN ? longParam(req, "employeeId") : user.getEmployeeId();
            t = new Timesheet();
            t.setEmployeeId(employeeId);
            t.setWeekStart(date(req, "weekStart"));
        }
        Date week = t.getWeekStart() != null ? t.getWeekStart() : DateUtil.mondayOf(DateUtil.today());
        t.getEntries().clear();
        for (int i = 0; i < 7; i++) {
            TimesheetEntry e = new TimesheetEntry();
            e.setWorkDate(DateUtil.addDays(week, i));
            String raw = req.getParameter("hours" + i);
            e.setHours(raw == null || raw.trim().isEmpty() ? java.math.BigDecimal.ZERO : Money.parse(raw));
            t.getEntries().add(e);
        }
        return t;
    }

    private void showForm(Timesheet t, ValidationErrors errors, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("timesheet", t);
        if (currentUser(req).getRole() == Role.ADMIN) {
            req.setAttribute("employees", new EmployeeDao().findActive());
        }
        if (errors != null) {
            req.setAttribute("errors", errors.getMap());
        }
        render("timesheets/form", req, resp);
    }
}
