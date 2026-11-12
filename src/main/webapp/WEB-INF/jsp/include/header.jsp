<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US" scope="request"/>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="me" value="${sessionScope.currentUser}" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title><c:out value="${pageTitle}"/> - Acme Payroll</title>
    <link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/smoothness/jquery-ui.css">
    <link rel="stylesheet" href="${ctx}/css/style.css">
    <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
    <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.min.js"></script>
    <script src="${ctx}/js/app.js"></script>
</head>
<body>
<div id="header">
    <div class="brand"><a href="${ctx}/dashboard">Acme Payroll</a></div>
    <c:if test="${not empty me}">
        <div class="userbox">
            <span id="current-user"><c:out value="${me.fullName}"/></span>
            <span class="role role-${me.role}">${me.role}</span>
            <form action="${ctx}/logout" method="post" class="inline">
                <button type="submit" id="logout-button" class="link">Sign out</button>
            </form>
        </div>
    </c:if>
</div>
<c:if test="${not empty me}">
<div id="menu">
    <ul>
        <li><a href="${ctx}/dashboard" class="${navSection == 'dashboard' ? 'active' : ''}">Dashboard</a></li>
        <c:if test="${me.admin or me.manager}">
            <li><a href="${ctx}/employees/" class="${navSection == 'employees' ? 'active' : ''}">Employees</a></li>
        </c:if>
        <c:if test="${me.employee}">
            <li><a href="${ctx}/profile" class="${navSection == 'profile' ? 'active' : ''}">My profile</a></li>
        </c:if>
        <li><a href="${ctx}/timesheets/" class="${navSection == 'timesheets' ? 'active' : ''}">Timesheets</a></li>
        <c:if test="${me.admin}">
            <li><a href="${ctx}/payruns/" class="${navSection == 'payruns' ? 'active' : ''}">Pay runs</a></li>
            <li><a href="${ctx}/departments/" class="${navSection == 'departments' ? 'active' : ''}">Departments</a></li>
        </c:if>
        <c:if test="${me.admin or me.manager}">
            <li><a href="${ctx}/reports/" class="${navSection == 'reports' ? 'active' : ''}">Reports</a></li>
        </c:if>
        <c:if test="${me.admin}">
            <li><a href="${ctx}/admin/taxes" class="${navSection == 'admin' ? 'active' : ''}">Settings</a></li>
        </c:if>
    </ul>
</div>
</c:if>
<div id="content">
<c:if test="${not empty flash}">
    <div class="message success" id="flash-message"><c:out value="${flash}"/></div>
</c:if>
<c:if test="${not empty flashError}">
    <div class="message error" id="flash-error"><c:out value="${flashError}"/></div>
</c:if>
