package com.acme.payroll.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.payroll.dao.DepartmentDao;
import com.acme.payroll.model.Department;
import com.acme.payroll.service.BusinessException;
import com.acme.payroll.service.DepartmentService;
import com.acme.payroll.service.ValidationErrors;

public class DepartmentServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private transient DepartmentDao departmentDao;
    private transient DepartmentService departmentService;

    @Override
    public void init() throws ServletException {
        departmentDao = new DepartmentDao();
        departmentService = new DepartmentService();
    }

    @Override
    protected void handleGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/".equals(action)) {
            req.setAttribute("departments", departmentDao.findAll());
            render("departments/list", req, resp);
        } else if ("/new".equals(action)) {
            showForm(new Department(), null, req, resp);
        } else if ("/edit".equals(action)) {
            Department d = departmentDao.findById(requiredId(req));
            if (d == null) {
                throw new NotFoundException();
            }
            showForm(d, null, req, resp);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    protected void handlePost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String user = currentUser(req).getEmail();
        if ("/save".equals(action)) {
            Department d = new Department();
            d.setId(longParam(req, "id"));
            d.setName(str(req, "name"));
            d.setCode(str(req, "code"));
            d.setCostCenter(str(req, "costCenter"));
            ValidationErrors errors = departmentService.save(d, user);
            if (!errors.isEmpty()) {
                showForm(d, errors, req, resp);
                return;
            }
            flash(req, "Department " + d.getName() + " saved.");
        } else if ("/delete".equals(action)) {
            try {
                departmentService.delete(requiredId(req), user);
                flash(req, "Department deleted.");
            } catch (BusinessException e) {
                flashError(req, e);
            }
        } else {
            throw new NotFoundException();
        }
        redirect("/departments", req, resp);
    }

    private void showForm(Department d, ValidationErrors errors, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("department", d);
        if (errors != null) {
            req.setAttribute("errors", errors.getMap());
        }
        render("departments/form", req, resp);
    }
}
