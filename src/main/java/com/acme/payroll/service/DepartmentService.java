package com.acme.payroll.service;

import com.acme.payroll.dao.AuditLogDao;
import com.acme.payroll.dao.DepartmentDao;
import com.acme.payroll.model.Department;

public class DepartmentService {

    private final DepartmentDao departmentDao;
    private final AuditLogDao auditDao;

    public DepartmentService() {
        this(new DepartmentDao(), new AuditLogDao());
    }

    public DepartmentService(DepartmentDao departmentDao, AuditLogDao auditDao) {
        this.departmentDao = departmentDao;
        this.auditDao = auditDao;
    }

    public ValidationErrors save(Department d, String user) {
        ValidationErrors errors = new ValidationErrors();
        if (d.getName() == null || d.getName().trim().isEmpty()) {
            errors.add("name", "Name is required.");
        } else {
            d.setName(d.getName().trim());
            Department other = departmentDao.findByName(d.getName());
            if (other != null && !other.getId().equals(d.getId())) {
                errors.add("name", "A department with this name already exists.");
            }
        }
        if (d.getCode() == null || !d.getCode().trim().matches("[A-Za-z]{2,10}")) {
            errors.add("code", "Code must be 2-10 letters.");
        } else {
            d.setCode(d.getCode().trim());
            Department other = departmentDao.findByCode(d.getCode());
            if (other != null && !other.getId().equals(d.getId())) {
                errors.add("code", "A department with this code already exists.");
            }
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        if (d.getId() == null) {
            departmentDao.create(d);
            auditDao.log(user, "CREATE", "Department", d.getId(), "Created department " + d.getName());
        } else {
            departmentDao.save(d);
            auditDao.log(user, "UPDATE", "Department", d.getId(), "Updated department " + d.getName());
        }
        return errors;
    }

    public void delete(Long id, String user) {
        Department d = departmentDao.findById(id);
        if (d == null) {
            throw new BusinessException("Department not found.");
        }
        if (departmentDao.countActiveEmployees(id) > 0) {
            throw new BusinessException("Cannot delete department \"" + d.getName()
                    + "\" because it has active employees.");
        }
        if (departmentDao.countAllEmployees(id) > 0) {
            throw new BusinessException("Cannot delete department \"" + d.getName()
                    + "\" because former employees still reference it.");
        }
        departmentDao.delete(id);
        auditDao.log(user, "DELETE", "Department", id, "Deleted department " + d.getName());
    }
}
