<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Timesheet" scope="request"/>
<c:set var="navSection" value="timesheets" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<div class="page-head">
    <h1>Timesheet - <c:out value="${timesheet.employeeName}"/></h1>
    <span class="status status-${timesheet.status}" id="timesheet-status">${timesheet.status}</span>
</div>

<table class="details">
    <tr><th>Week starting</th><td><fmt:formatDate value="${timesheet.weekStart}" pattern="EEEE, yyyy-MM-dd"/></td></tr>
    <tr><th>Submitted</th><td><fmt:formatDate value="${timesheet.submittedAt}" pattern="yyyy-MM-dd HH:mm"/></td></tr>
    <c:if test="${not empty timesheet.decidedBy}">
        <tr><th>Decided by</th><td><c:out value="${timesheet.decidedBy}"/> on <fmt:formatDate value="${timesheet.decidedAt}" pattern="yyyy-MM-dd HH:mm"/></td></tr>
    </c:if>
    <c:if test="${not empty timesheet.rejectReason}">
        <tr><th>Reject reason</th><td id="reject-reason"><c:out value="${timesheet.rejectReason}"/></td></tr>
    </c:if>
</table>

<table class="grid hours" id="hours-table">
    <thead>
    <tr>
        <c:forEach items="${timesheet.entries}" var="e">
            <th><fmt:formatDate value="${e.workDate}" pattern="EEE MM/dd"/></th>
        </c:forEach>
        <th>Total</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <c:forEach items="${timesheet.entries}" var="e">
            <td class="num"><fmt:formatNumber value="${e.hours}" maxFractionDigits="2"/></td>
        </c:forEach>
        <td class="num"><strong><fmt:formatNumber value="${timesheet.totalHours}" maxFractionDigits="2"/></strong></td>
    </tr>
    </tbody>
</table>

<div class="actions">
    <c:if test="${canEdit}">
        <a href="${ctx}/timesheets/edit?id=${timesheet.id}" class="button" id="edit-timesheet">Edit</a>
        <form method="post" action="${ctx}/timesheets/submit" class="inline">
            <input type="hidden" name="id" value="${timesheet.id}">
            <button type="submit" class="primary" id="submit-timesheet">Submit for approval</button>
        </form>
    </c:if>
    <c:if test="${canDecide}">
        <form method="post" action="${ctx}/timesheets/approve" class="inline">
            <input type="hidden" name="id" value="${timesheet.id}">
            <button type="submit" class="primary" id="approve-timesheet">Approve</button>
        </form>
        <button type="button" class="danger" id="reject-timesheet">Reject...</button>
    </c:if>
    <a href="${ctx}/timesheets/">Back to list</a>
</div>

<c:if test="${canDecide}">
<div id="reject-dialog" title="Reject timesheet" style="display:none;">
    <form method="post" action="${ctx}/timesheets/reject" id="reject-form">
        <input type="hidden" name="id" value="${timesheet.id}">
        <label for="reject-reason-input">Reason (required)</label>
        <textarea id="reject-reason-input" name="reason" rows="3" cols="40"></textarea>
        <span class="field-error" id="reject-reason-error" style="display:none;">Please enter a reason.</span>
    </form>
</div>
</c:if>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
