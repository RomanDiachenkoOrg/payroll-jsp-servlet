package com.acme.payroll.web.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.acme.payroll.model.Role;
import com.acme.payroll.model.User;
import com.acme.payroll.web.servlet.BaseServlet;

/**
 * Redirects anonymous requests to the login page and enforces the
 * coarse-grained role rules per URL prefix. Finer checks (own department,
 * own timesheet) are done in the servlets.
 */
public class AuthFilter implements Filter {

    private static final String[] PUBLIC = {"/login", "/css/", "/js/", "/images/", "/favicon.ico"};

    private final Map<String, Set<Role>> rules = new LinkedHashMap<String, Set<Role>>();

    @Override
    public void init(FilterConfig config) throws ServletException {
        rules.put("/admin", EnumSet.of(Role.ADMIN));
        rules.put("/payruns", EnumSet.of(Role.ADMIN));
        rules.put("/departments", EnumSet.of(Role.ADMIN));
        rules.put("/reports", EnumSet.of(Role.ADMIN, Role.MANAGER));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        for (String p : PUBLIC) {
            if (path.equals(p) || (p.endsWith("/") && path.startsWith(p))) {
                chain.doFilter(request, response);
                return;
            }
        }

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(BaseServlet.SESSION_USER);
        if (user == null) {
            String target = path;
            if (req.getQueryString() != null) {
                target += "?" + req.getQueryString();
            }
            String next = "GET".equals(req.getMethod()) && !"/".equals(path)
                    ? "?next=" + URLEncoder.encode(target, "UTF-8") : "";
            resp.sendRedirect(req.getContextPath() + "/login" + next);
            return;
        }

        for (Map.Entry<String, Set<Role>> rule : rules.entrySet()) {
            if (path.equals(rule.getKey()) || path.startsWith(rule.getKey() + "/")) {
                if (!rule.getValue().contains(user.getRole())) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        }

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        rules.clear();
    }
}
