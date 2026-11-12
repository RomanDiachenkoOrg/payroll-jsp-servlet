package com.acme.payroll.web.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.payroll.dao.AuditLogDao;
import com.acme.payroll.dao.DeductionDao;
import com.acme.payroll.dao.SettingsDao;
import com.acme.payroll.dao.UserDao;
import com.acme.payroll.model.TaxBracket;
import com.acme.payroll.service.ValidationErrors;

/**
 * /admin/taxes   tax brackets and contribution rates
 * /admin/users   user accounts
 * /admin/audit   audit log with filter
 */
public class AdminServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    protected void handleGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/".equals(action) || "/taxes".equals(action)) {
            showTaxes(req, resp);
        } else if ("/users".equals(action)) {
            req.setAttribute("users", new UserDao().findAll());
            render("admin/users", req, resp);
        } else if ("/audit".equals(action)) {
            AuditLogDao dao = new AuditLogDao();
            req.setAttribute("entries", dao.find(str(req, "user"), str(req, "action"), str(req, "entity"), 200));
            req.setAttribute("actions", dao.findActions());
            render("admin/audit", req, resp);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    protected void handlePost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String user = currentUser(req).getEmail();
        if ("/taxes".equals(action)) {
            saveTaxes(req, resp, user);
        } else if ("/users/toggle".equals(action)) {
            Long id = requiredId(req);
            if (id.equals(currentUser(req).getId())) {
                flashError(req, "You cannot disable your own account.");
            } else {
                boolean active = bool(req, "active");
                new UserDao().setActive(id, active);
                new AuditLogDao().log(user, "UPDATE", "User", id, active ? "Enabled user" : "Disabled user");
                flash(req, active ? "User enabled." : "User disabled.");
            }
            redirect("/admin/users", req, resp);
        } else {
            throw new NotFoundException();
        }
    }

    private void showTaxes(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SettingsDao settings = new SettingsDao();
        req.setAttribute("brackets", settings.findBrackets());
        req.setAttribute("socialRate", settings.getDecimal(SettingsDao.SOCIAL_RATE, "0.05").multiply(HUNDRED)
                .stripTrailingZeros().toPlainString());
        req.setAttribute("pensionRate", settings.getDecimal(SettingsDao.PENSION_RATE, "0.03").multiply(HUNDRED)
                .stripTrailingZeros().toPlainString());
        req.setAttribute("deductionTypes", new DeductionDao().findTypes());
        render("admin/taxes", req, resp);
    }

    private void saveTaxes(HttpServletRequest req, HttpServletResponse resp, String user)
            throws ServletException, IOException {
        ValidationErrors errors = new ValidationErrors();
        String[] lowers = req.getParameterValues("lower");
        String[] uppers = req.getParameterValues("upper");
        String[] rates = req.getParameterValues("rate");
        List<TaxBracket> brackets = new ArrayList<TaxBracket>();
        if (lowers == null || uppers == null || rates == null) {
            errors.add("brackets", "At least one tax bracket is required.");
        } else {
            BigDecimal expectedLower = BigDecimal.ZERO;
            for (int i = 0; i < lowers.length; i++) {
                if (lowers[i].trim().isEmpty() && rates[i].trim().isEmpty()) {
                    continue;
                }
                TaxBracket b = new TaxBracket();
                b.setLowerBound(com.acme.payroll.util.Money.parse(lowers[i]));
                b.setUpperBound(com.acme.payroll.util.Money.parse(uppers[i]));
                BigDecimal pct = com.acme.payroll.util.Money.parse(rates[i]);
                if (b.getLowerBound() == null || pct == null || pct.signum() < 0 || pct.compareTo(HUNDRED) > 0) {
                    errors.add("brackets", "Row " + (i + 1) + ": enter a lower bound and a rate between 0 and 100.");
                    continue;
                }
                if (b.getLowerBound().compareTo(expectedLower) != 0) {
                    errors.add("brackets", "Row " + (i + 1) + ": brackets must be contiguous (expected lower bound "
                            + expectedLower.toPlainString() + ").");
                }
                if (b.getUpperBound() != null && b.getUpperBound().compareTo(b.getLowerBound()) <= 0) {
                    errors.add("brackets", "Row " + (i + 1) + ": upper bound must be greater than lower bound.");
                }
                b.setRate(pct.divide(HUNDRED, 4, java.math.RoundingMode.HALF_UP));
                brackets.add(b);
                expectedLower = b.getUpperBound() == null ? expectedLower : b.getUpperBound();
            }
            if (!brackets.isEmpty() && brackets.get(brackets.size() - 1).getUpperBound() != null) {
                errors.add("brackets", "The last bracket must have no upper bound.");
            }
        }
        BigDecimal social = com.acme.payroll.util.Money.parse(req.getParameter("socialRate"));
        BigDecimal pension = com.acme.payroll.util.Money.parse(req.getParameter("pensionRate"));
        if (social == null || social.signum() < 0 || social.compareTo(HUNDRED) > 0) {
            errors.add("socialRate", "Social contribution must be between 0 and 100.");
        }
        if (pension == null || pension.signum() < 0 || pension.compareTo(HUNDRED) > 0) {
            errors.add("pensionRate", "Pension rate must be between 0 and 100.");
        }
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors.getMap());
            showTaxes(req, resp);
            return;
        }
        SettingsDao settings = new SettingsDao();
        settings.replaceBrackets(brackets);
        settings.set(SettingsDao.SOCIAL_RATE, social.divide(HUNDRED).toPlainString());
        settings.set(SettingsDao.PENSION_RATE, pension.divide(HUNDRED).toPlainString());
        new AuditLogDao().log(user, "UPDATE", "Settings", null, "Updated tax brackets and contribution rates");
        flash(req, "Tax settings saved. They apply to pay runs calculated from now on.");
        redirect("/admin/taxes", req, resp);
    }
}
