package com.acme.payroll.web.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acme.payroll.model.Role;
import com.acme.payroll.model.User;
import com.acme.payroll.service.BusinessException;
import com.acme.payroll.util.DateUtil;
import com.acme.payroll.util.Money;

/**
 * Base class for the controllers. Sub-classes are mapped to a prefix such as
 * {@code /employees/*} and dispatch on the path info ("/", "/edit", "/save"...).
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SESSION_USER = "currentUser";
    private static final String FLASH = "flash";
    private static final String FLASH_ERROR = "flashError";

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        dispatch(req, resp, false);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        dispatch(req, resp, true);
    }

    private void dispatch(HttpServletRequest req, HttpServletResponse resp, boolean post)
            throws ServletException, IOException {
        String action = req.getPathInfo();
        if (action == null || action.isEmpty()) {
            action = "/";
        }
        moveFlash(req);
        try {
            if (post) {
                handlePost(action, req, resp);
            } else {
                handleGet(action, req, resp);
            }
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (ForbiddenException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    protected abstract void handleGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException;

    protected void handlePost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    protected void render(String view, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/" + view + ".jsp").forward(req, resp);
    }

    protected void redirect(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute(SESSION_USER);
    }

    protected void requireRole(HttpServletRequest req, Role... roles) {
        User u = currentUser(req);
        if (u != null) {
            for (Role r : roles) {
                if (u.getRole() == r) {
                    return;
                }
            }
        }
        throw new ForbiddenException();
    }

    protected void flash(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH, message);
    }

    protected void flashError(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_ERROR, message);
    }

    protected void flashError(HttpServletRequest req, BusinessException e) {
        flashError(req, e.getMessage());
    }

    private void moveFlash(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return;
        }
        for (String key : new String[] {FLASH, FLASH_ERROR}) {
            Object v = s.getAttribute(key);
            if (v != null) {
                req.setAttribute(key, v);
                s.removeAttribute(key);
            }
        }
    }

    // --- parameter helpers -------------------------------------------------

    protected static String str(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }

    protected static Long longParam(HttpServletRequest req, String name) {
        String v = str(req, name);
        if (v == null || v.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected static Long requiredId(HttpServletRequest req) {
        Long id = longParam(req, "id");
        if (id == null) {
            throw new NotFoundException();
        }
        return id;
    }

    protected static int intParam(HttpServletRequest req, String name, int defaultValue) {
        Long v = longParam(req, name);
        return v == null ? defaultValue : v.intValue();
    }

    protected static BigDecimal decimal(HttpServletRequest req, String name) {
        return Money.parse(req.getParameter(name));
    }

    protected static Date date(HttpServletRequest req, String name) {
        return DateUtil.parse(req.getParameter(name));
    }

    protected static boolean bool(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v != null && ("on".equals(v) || "true".equals(v) || "1".equals(v));
    }

    /** Thrown to answer 404. */
    protected static class NotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    /** Thrown to answer 403. */
    protected static class ForbiddenException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
