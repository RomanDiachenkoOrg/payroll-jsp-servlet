package com.acme.payroll.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acme.payroll.dao.AuditLogDao;
import com.acme.payroll.dao.UserDao;
import com.acme.payroll.model.User;
import com.acme.payroll.util.PasswordUtil;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;
    private final AuditLogDao auditDao;

    public AuthService() {
        this(new UserDao(), new AuditLogDao());
    }

    public AuthService(UserDao userDao, AuditLogDao auditDao) {
        this.userDao = userDao;
        this.auditDao = auditDao;
    }

    /**
     * @return the user, or {@code null} when the email/password combination is wrong
     *         or the account is disabled
     */
    public User authenticate(String email, String password) {
        if (email == null || password == null) {
            return null;
        }
        User user = userDao.findByEmail(email.trim());
        if (user == null || !user.isActive()
                || !PasswordUtil.matches(password, user.getSalt(), user.getPasswordHash())) {
            log.info("Failed login for {}", email);
            auditDao.log(email.trim(), "LOGIN_FAILED", "User", user == null ? null : user.getId(),
                    "Invalid credentials");
            return null;
        }
        userDao.touchLastLogin(user.getId());
        auditDao.log(user.getEmail(), "LOGIN", "User", user.getId(), "Successful login");
        user.setPasswordHash(null);
        user.setSalt(null);
        return user;
    }
}
