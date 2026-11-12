<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Timesheets" scope="request"/>
<c:set var="navSection" value="timesheets" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<div class="page-head">
    <h1>Timesheets</h1>
    <c:if test="${not empty me.employeeId or me.admin}">
        <a href="${ctx}/timesheets/new" class="button primary" id="new-timesheet">New timesheet</a>
    </c:if>
</div>

<form method="get" action="${ctx}/timesheets/" class="filter-bar" id="timesheet-filter">
    <label for="status">Status</label>
    <select id="status" name="status" onchange="this.form.submit()">
        <option value="">All</option>
        <c:forEach items="${statuses}" var="s">
            <option value="${s}" ${status == s ? 'selected' : ''}>${s}</option>
        </c:forEach>
    </select>
    <noscript><button type="submit">Filter</button></noscript>
</form>

<table class="grid" id="timesheets-table">
    <thead>
    <tr><th>Week of</th><th>Employee</th><th class="num">Hours</th><th>Status</th><th>Submitted</th><th>Decided by</th><th></th></tr>
    </thead>
    <tbody>
    <c:forEach items="${timesheets}" var="t">
        <tr>
            <td><fmt:formatDate value="${t.weekStart}" pattern="yyyy-MM-dd"/></td>
            <td><c:out value="${t.employeeName}"/></td>
            <td class="num"><fmt:formatNumber value="${t.totalHours}" minFractionDigits="1" maxFractionDigits="2"/></td>
            <td><span class="status status-${t.status}">${t.status}</span></td>
            <td><fmt:formatDate value="${t.submittedAt}" pattern="yyyy-MM-dd HH:mm"/></td>
            <td><c:out value="${t.decidedBy}"/></td>
            <td><a href="${ctx}/timesheets/view?id=${t.id}">Open</a></td>
        </tr>
    </c:forEach>
    <c:if test="${empty timesheets}">
        <tr><td colspan="7" class="empty">No timesheets found.</td></tr>
    </c:if>
    </tbody>
</table>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
