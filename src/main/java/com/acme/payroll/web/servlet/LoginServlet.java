package com.acme.payroll.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.acme.payroll.model.User;
import com.acme.payroll.service.AuthService;
import com.acme.payroll.service.ValidationErrors;

public class LoginServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private transient AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }

    @Override
    protected void handleGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (currentUser(req) != null) {
            redirect("/dashboard", req, resp);
            return;
        }
        if ("1".equals(req.getParameter("loggedOut"))) {
            req.setAttribute("flash", "You have been signed out.");
        }
        if ("1".equals(req.getParameter("expired"))) {
            req.setAttribute("flashError", "Your session has expired. Please sign in again.");
        }
        req.setAttribute("next", req.getParameter("next"));
        render("login", req, resp);
    }

    @Override
    protected void handlePost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = str(req, "email");
        String password = req.getParameter("password");
        ValidationErrors errors = new ValidationErrors();
        if (email == null || email.isEmpty()) {
            errors.add("email", "Email is required.");
        } else if (!email.contains("@")) {
            errors.add("email", "Enter a valid email address.");
        }
        if (password == null || password.isEmpty()) {
            errors.add("password", "Password is required.");
        }
        User user = null;
        if (errors.isEmpty()) {
            user = authService.authenticate(email, password);
            if (user == null) {
                req.setAttribute("loginError", "Invalid email or password.");
            }
        }
        if (user == null) {
            req.setAttribute("errors", errors.getMap());
            req.setAttribute("email", email);
            req.setAttribute("next", req.getParameter("next"));
            resp.setStatus(HttpServletResponse.SC_OK);
            render("login", req, resp);
            return;
        }
        // new session id after login
        HttpSession old = req.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = req.getSession(true);
        session.setAttribute(SESSION_USER, user);
        String next = req.getParameter("next");
        if (next != null && next.startsWith("/") && !next.startsWith("//")) {
            redirect(next, req, resp);
        } else {
            redirect("/dashboard", req, resp);
        }
    }
}
