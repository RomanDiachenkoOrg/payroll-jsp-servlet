package com.acme.payroll.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.acme.payroll.dao.AuditLogDao;
import com.acme.payroll.model.User;

public class LogoutServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        logout(req, resp);
    }

    @Override
    protected void handlePost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        logout(req, resp);
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User u = (User) session.getAttribute(SESSION_USER);
            if (u != null) {
                new AuditLogDao().log(u.getEmail(), "LOGOUT", "User", u.getId(), "Signed out");
            }
            session.invalidate();
        }
        redirect("/login?loggedOut=1", req, resp);
    }
}
