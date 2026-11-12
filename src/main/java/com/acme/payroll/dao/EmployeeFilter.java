package com.acme.payroll.dao;

/**
 * Search criteria for the employee list screen.
 */
public class EmployeeFilter {

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";
    public static final String STATUS_ALL = "all";

    private String query;
    private Long departmentId;
    private String status = STATUS_ACTIVE;
    private String sort = "name";
    private boolean descending;
    private int page = 1;
    private int pageSize = 10;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public boolean isDescending() {
        return descending;
    }

    public void setDescending(boolean descending) {
        this.descending = descending;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page < 1 ? 1 : page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** Maps the sort key coming from the request to a safe ORDER BY clause. */
    public String orderBy() {
        String col;
        if ("no".equals(sort)) {
            col = "e.employee_no";
        } else if ("department".equals(sort)) {
            col = "d.name";
        } else if ("hireDate".equals(sort)) {
            col = "e.hire_date";
        } else if ("title".equals(sort)) {
            col = "e.job_title";
        } else {
            col = "e.last_name " + (descending ? "DESC" : "ASC") + ", e.first_name";
        }
        return col + (descending ? " DESC" : " ASC") + ", e.id";
    }
}
