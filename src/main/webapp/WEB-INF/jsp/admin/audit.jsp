<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Audit log" scope="request"/>
<c:set var="navSection" value="admin" scope="request"/>
<c:set var="adminTab" value="audit"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<h1>Settings</h1>
<%@ include file="subnav.jspf" %>

<form method="get" action="${ctx}/admin/audit" class="filter-bar" id="audit-filter">
    <label for="user">User</label>
    <input type="text" id="user" name="user" value="<c:out value='${param.user}'/>">
    <label for="action">Action</label>
    <select id="action" name="action">
        <option value="">All</option>
        <c:forEach items="${actions}" var="a">
            <option value="${a}" ${param.action == a ? 'selected' : ''}>${a}</option>
        </c:forEach>
    </select>
    <label for="entity">Entity</label>
    <select id="entity" name="entity">
        <option value="">All</option>
        <c:forEach items="Department,Employee,PayRun,Settings,Timesheet,User" var="e">
            <option value="${e}" ${param.entity == e ? 'selected' : ''}>${e}</option>
        </c:forEach>
    </select>
    <button type="submit" id="audit-filter-button">Filter</button>
</form>

<table class="grid" id="audit-table">
    <thead><tr><th>Time</th><th>User</th><th>Action</th><th>Entity</th><th>Details</th></tr></thead>
    <tbody>
    <c:forEach items="${entries}" var="a">
        <tr>
            <td><fmt:formatDate value="${a.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            <td><c:out value="${a.userEmail}"/></td>
            <td>${a.action}</td>
            <td>${a.entity}<c:if test="${not empty a.entityId}"> #${a.entityId}</c:if></td>
            <td><c:out value="${a.details}"/></td>
        </tr>
    </c:forEach>
    <c:if test="${empty entries}"><tr><td colspan="5" class="empty">No entries.</td></tr></c:if>
    </tbody>
</table>
<p class="hint">Showing the latest 200 matching entries.</p>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
