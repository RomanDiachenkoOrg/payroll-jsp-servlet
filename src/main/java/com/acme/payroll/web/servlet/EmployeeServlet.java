package com.acme.payroll.web.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.payroll.dao.DeductionDao;
import com.acme.payroll.dao.DepartmentDao;
import com.acme.payroll.dao.EmployeeDao;
import com.acme.payroll.dao.EmployeeFilter;
import com.acme.payroll.dao.PayslipDao;
import com.acme.payroll.dao.SalaryHistoryDao;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayType;
import com.acme.payroll.model.Role;
import com.acme.payroll.model.User;
import com.acme.payroll.service.BusinessException;
import com.acme.payroll.service.EmployeeService;
import com.acme.payroll.service.ValidationErrors;
import com.acme.payroll.util.DateUtil;

/**
 * /employees            list (admin, manager)
 * /employees/view?id=   detail (admin, manager, the employee himself)
 * /employees/new|edit   form (admin)
 * /employees/save       POST (admin)
 * /employees/deactivate POST (admin)
 * /employees/raise      POST (admin)
 * /profile              current employee's own record
 */
public class EmployeeServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private transient EmployeeDao employeeDao;
    private transient DepartmentDao departmentDao;
    private transient EmployeeService employeeService;

    @Override
    public void init() throws ServletException {
        employeeDao = new EmployeeDao();
        departmentDao = new DepartmentDao();
        employeeService = new EmployeeService();
    }

    @Override
    protected void handleGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/profile".equals(req.getServletPath())) {
            User u = currentUser(req);
            if (u.getEmployeeId() == null) {
                throw new NotFoundException();
            }
            showDetail(u.getEmployeeId(), req, resp);
        } else if ("/".equals(action)) {
            requireRole(req, Role.ADMIN, Role.MANAGER);
            list(req, resp);
        } else if ("/view".equals(action)) {
            showDetail(requiredId(req), req, resp);
        } else if ("/new".equals(action)) {
            requireRole(req, Role.ADMIN);
            Employee e = new Employee();
            e.setPayType(PayType.SALARIED);
            e.setHireDate(DateUtil.today());
            e.setActive(true);
            showForm(e, null, req, resp);
        } else if ("/edit".equals(action)) {
            requireRole(req, Role.ADMIN);
            Employee e = employeeDao.findById(requiredId(req));
            if (e == null) {
                throw new NotFoundException();
            }
            showForm(e, null, req, resp);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    protected void handlePost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        requireRole(req, Role.ADMIN);
        String user = currentUser(req).getEmail();
        if ("/save".equals(action)) {
            Employee e = bind(req);
            ValidationErrors errors = employeeService.save(e, user);
            if (!errors.isEmpty()) {
                showForm(e, errors, req, resp);
                return;
            }
            flash(req, "Employee " + e.getFullName() + " saved.");
            redirect("/employees/view?id=" + e.getId(), req, resp);
        } else if ("/deactivate".equals(action) || "/activate".equals(action)) {
            Long id = requiredId(req);
            try {
                if ("/deactivate".equals(action)) {
                    employeeService.deactivate(id, user);
                    flash(req, "Employee deactivated.");
                } else {
                    employeeService.reactivate(id, user);
                    flash(req, "Employee reactivated.");
                }
            } catch (BusinessException ex) {
                flashError(req, ex);
            }
            redirect("/employees/view?id=" + id, req, resp);
        } else if ("/raise".equals(action)) {
            Long id = requiredId(req);
            BigDecimal amount = decimal(req, "amount");
            Date effective = date(req, "effectiveDate");
            ValidationErrors errors = employeeService.addRaise(id, amount, effective, str(req, "reason"), user);
            if (!errors.isEmpty()) {
                req.setAttribute("raiseErrors", errors.getMap());
                req.setAttribute("raiseAmount", req.getParameter("amount"));
                req.setAttribute("raiseDate", req.getParameter("effectiveDate"));
                req.setAttribute("raiseReason", req.getParameter("reason"));
                showDetail(id, req, resp);
                return;
            }
            flash(req, "Salary change recorded.");
            redirect("/employees/view?id=" + id, req, resp);
        } else {
            throw new NotFoundException();
        }
    }

    private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        EmployeeFilter f = new EmployeeFilter();
        f.setQuery(str(req, "q"));
        f.setDepartmentId(longParam(req, "department"));
        String status = str(req, "status");
        if (status != null && !status.isEmpty()) {
            f.setStatus(status);
        }
        String sort = str(req, "sort");
        if (sort != null && !sort.isEmpty()) {
            f.setSort(sort);
        }
        f.setDescending("desc".equals(req.getParameter("dir")));
        f.setPage(intParam(req, "page", 1));
        req.setAttribute("filter", f);
        req.setAttribute("result", employeeDao.search(f));
        req.setAttribute("departments", departmentDao.findAll());
        render("employees/list", req, resp);
    }

    private void showDetail(Long id, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Employee e = employeeDao.findById(id);
        if (e == null) {
            throw new NotFoundException();
        }
        User u = currentUser(req);
        boolean own = id.equals(u.getEmployeeId());
        if (u.getRole() == Role.EMPLOYEE && !own) {
            throw new ForbiddenException();
        }
        req.setAttribute("employee", e);
        req.setAttribute("salaryHistory", new SalaryHistoryDao().findByEmployee(id));
        req.setAttribute("payslips", new PayslipDao().findByEmployee(id));
        req.setAttribute("deductions", new DeductionDao().findForEmployee(id));
        req.setAttribute("today", DateUtil.format(DateUtil.today()));
        render("employees/view", req, resp);
    }

    private void showForm(Employee e, ValidationErrors errors, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("employee", e);
        req.setAttribute("departments", departmentDao.findAll());
        req.setAttribute("payTypes", PayType.values());
        if (errors != null) {
            req.setAttribute("errors", errors.getMap());
            req.setAttribute("hireDateText", req.getParameter("hireDate"));
        }
        render("employees/form", req, resp);
    }

    private Employee bind(HttpServletRequest req) {
        Long id = longParam(req, "id");
        Employee e = id == null ? new Employee() : employeeDao.findById(id);
        if (e == null) {
            throw new NotFoundException();
        }
        e.setFirstName(str(req, "firstName"));
        e.setLastName(str(req, "lastName"));
        e.setEmail(str(req, "email"));
        e.setPhone(str(req, "phone"));
        e.setJobTitle(str(req, "jobTitle"));
        e.setDepartmentId(longParam(req, "departmentId"));
        String payType = str(req, "payType");
        e.setPayType(payType == null || payType.isEmpty() ? null : PayType.valueOf(payType));
        e.setAnnualSalary(decimal(req, "annualSalary"));
        e.setHourlyRate(decimal(req, "hourlyRate"));
        e.setPensionOptIn(bool(req, "pensionOptIn"));
        e.setHireDate(date(req, "hireDate"));
        return e;
    }
}
