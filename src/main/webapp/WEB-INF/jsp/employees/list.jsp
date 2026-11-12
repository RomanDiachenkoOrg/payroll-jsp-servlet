<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Employees" scope="request"/>
<c:set var="navSection" value="employees" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<div class="page-head">
    <h1>Employees</h1>
    <c:if test="${me.admin}">
        <a href="${ctx}/employees/new" class="button primary" id="new-employee">New employee</a>
    </c:if>
</div>

<form method="get" action="${ctx}/employees/" class="filter-bar" id="employee-filter">
    <label for="q">Search</label>
    <input type="text" id="q" name="q" value="<c:out value='${filter.query}'/>" placeholder="Name, email or number">
    <label for="department">Department</label>
    <select id="department" name="department">
        <option value="">All departments</option>
        <c:forEach items="${departments}" var="d">
            <option value="${d.id}" ${filter.departmentId == d.id ? 'selected' : ''}><c:out value="${d.name}"/></option>
        </c:forEach>
    </select>
    <label for="status">Status</label>
    <select id="status" name="status">
        <option value="active" ${filter.status == 'active' ? 'selected' : ''}>Active</option>
        <option value="inactive" ${filter.status == 'inactive' ? 'selected' : ''}>Inactive</option>
        <option value="all" ${filter.status == 'all' ? 'selected' : ''}>All</option>
    </select>
    <input type="hidden" name="sort" value="${filter.sort}">
    <input type="hidden" name="dir" value="${filter.descending ? 'desc' : 'asc'}">
    <button type="submit" id="filter-button">Filter</button>
    <a href="${ctx}/employees/">Reset</a>
</form>

<c:url var="baseUrl" value="/employees/">
    <c:param name="q" value="${filter.query}"/>
    <c:param name="department" value="${filter.departmentId}"/>
    <c:param name="status" value="${filter.status}"/>
</c:url>

<table class="grid" id="employees-table">
    <thead>
    <tr>
        <c:forEach items="no:No.,name:Name,department:Department,title:Job title,hireDate:Hire date" var="col">
            <c:set var="key" value="${fn:substringBefore(col, ':')}"/>
            <c:set var="label" value="${fn:substringAfter(col, ':')}"/>
            <th>
                <a href="${baseUrl}&amp;sort=${key}&amp;dir=${filter.sort == key and not filter.descending ? 'desc' : 'asc'}">${label}</a>
                <c:if test="${filter.sort == key}">${filter.descending ? '&#9660;' : '&#9650;'}</c:if>
            </th>
        </c:forEach>
        <th>Pay type</th>
        <th>Status</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${result.items}" var="e">
        <tr class="${e.active ? '' : 'inactive'}">
            <td>${e.employeeNo}</td>
            <td><a href="${ctx}/employees/view?id=${e.id}"><c:out value="${e.lastName}, ${e.firstName}"/></a>
                <div class="sub"><c:out value="${e.email}"/></div></td>
            <td><c:out value="${e.departmentName}"/></td>
            <td><c:out value="${e.jobTitle}"/></td>
            <td><fmt:formatDate value="${e.hireDate}" pattern="yyyy-MM-dd"/></td>
            <td>${e.payType.label}</td>
            <td><span class="status ${e.active ? 'status-active' : 'status-inactive'}">${e.active ? 'Active' : 'Inactive'}</span></td>
        </tr>
    </c:forEach>
    <c:if test="${empty result.items}">
        <tr><td colspan="7" class="empty">No employees match the filter.</td></tr>
    </c:if>
    </tbody>
</table>

<div class="pager" id="pager">
    <span class="pager-info">Showing ${result.firstItem}-${result.lastItem} of ${result.totalItems}</span>
    <c:if test="${result.hasPrevious}">
        <a href="${baseUrl}&amp;sort=${filter.sort}&amp;dir=${filter.descending ? 'desc' : 'asc'}&amp;page=${result.page - 1}" id="prev-page">&laquo; Previous</a>
    </c:if>
    <span>Page ${result.page} of ${result.totalPages}</span>
    <c:if test="${result.hasNext}">
        <a href="${baseUrl}&amp;sort=${filter.sort}&amp;dir=${filter.descending ? 'desc' : 'asc'}&amp;page=${result.page + 1}" id="next-page">Next &raquo;</a>
    </c:if>
</div>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
