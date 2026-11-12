<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Pay run ${payRun.periodLabel}" scope="request"/>
<c:set var="navSection" value="payruns" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<div class="page-head">
    <h1>Pay run ${payRun.periodLabel}
        <span class="status status-${payRun.status}" id="payrun-status">${payRun.status}</span>
        <c:if test="${payRun.locked}"><span class="locked" title="Paid pay runs are locked">&#128274; Locked</span></c:if>
    </h1>
</div>

<table class="details" id="payrun-details">
    <tr><th>Period</th><td><fmt:formatDate value="${payRun.periodStart}" pattern="yyyy-MM-dd"/> &ndash; <fmt:formatDate value="${payRun.periodEnd}" pattern="yyyy-MM-dd"/></td></tr>
    <tr><th>Pay date</th><td><fmt:formatDate value="${payRun.payDate}" pattern="yyyy-MM-dd"/></td></tr>
    <tr><th>Created</th><td><c:out value="${payRun.createdBy}"/> <fmt:formatDate value="${payRun.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td></tr>
    <c:if test="${not empty payRun.calculatedAt}"><tr><th>Calculated</th><td><fmt:formatDate value="${payRun.calculatedAt}" pattern="yyyy-MM-dd HH:mm"/></td></tr></c:if>
    <c:if test="${not empty payRun.approvedBy}"><tr><th>Approved</th><td><c:out value="${payRun.approvedBy}"/> <fmt:formatDate value="${payRun.approvedAt}" pattern="yyyy-MM-dd HH:mm"/></td></tr></c:if>
    <c:if test="${not empty payRun.paidAt}"><tr><th>Paid</th><td><fmt:formatDate value="${payRun.paidAt}" pattern="yyyy-MM-dd HH:mm"/></td></tr></c:if>
    <tr><th>Totals</th><td>Gross <fmt:formatNumber value="${payRun.totalGross}" type="currency" currencyCode="USD"/>,
        net <fmt:formatNumber value="${payRun.totalNet}" type="currency" currencyCode="USD"/> (${payRun.payslipCount} payslips)</td></tr>
</table>

<div class="actions" id="payrun-actions">
    <c:if test="${payRun.status == 'DRAFT' or payRun.status == 'CALCULATED'}">
        <form method="post" action="${ctx}/payruns/calculate" class="inline">
            <input type="hidden" name="id" value="${payRun.id}">
            <button type="submit" class="primary" id="calculate-payrun">${payRun.status == 'DRAFT' ? 'Calculate' : 'Recalculate'}</button>
        </form>
    </c:if>
    <c:if test="${payRun.status == 'CALCULATED'}">
        <form method="post" action="${ctx}/payruns/approve" class="inline confirm" data-confirm="Approve this pay run? Payslips can no longer be recalculated.">
            <input type="hidden" name="id" value="${payRun.id}">
            <button type="submit" id="approve-payrun">Approve</button>
        </form>
    </c:if>
    <c:if test="${not payRun.locked and payRun.status != 'DRAFT'}">
        <form method="post" action="${ctx}/payruns/pay" class="inline confirm" data-confirm="Mark this pay run as paid? It will be locked.">
            <input type="hidden" name="id" value="${payRun.id}">
            <button type="submit" id="mark-paid">Mark paid</button>
        </form>
    </c:if>
    <c:if test="${payRun.payslipCount > 0}">
        <a href="${ctx}/payruns/export?id=${payRun.id}" class="button" id="export-payrun">Export CSV</a>
    </c:if>
    <c:if test="${payRun.status == 'DRAFT' or payRun.status == 'CALCULATED'}">
        <form method="post" action="${ctx}/payruns/delete" class="inline confirm" data-confirm="Delete this pay run and all its payslips?">
            <input type="hidden" name="id" value="${payRun.id}">
            <button type="submit" class="danger" id="delete-payrun">Delete</button>
        </form>
    </c:if>
</div>

<h2>Payslips</h2>
<table class="grid" id="payslips-table">
    <thead>
    <tr><th>No.</th><th>Employee</th><th>Department</th><th class="num">Hours (reg/OT)</th><th class="num">Gross</th>
        <th class="num">Tax</th><th class="num">Social</th><th class="num">Pension</th><th class="num">Other</th><th class="num">Net</th><th></th></tr>
    </thead>
    <tbody>
    <c:forEach items="${payslips}" var="p">
        <tr>
            <td>${p.employeeNo}</td>
            <td><c:out value="${p.employeeName}"/></td>
            <td><c:out value="${p.departmentName}"/></td>
            <td class="num"><c:if test="${p.regularHours > 0}">${p.regularHours} / ${p.overtimeHours}</c:if></td>
            <td class="num"><fmt:formatNumber value="${p.gross}" minFractionDigits="2"/></td>
            <td class="num"><fmt:formatNumber value="${p.incomeTax}" minFractionDigits="2"/></td>
            <td class="num"><fmt:formatNumber value="${p.social}" minFractionDigits="2"/></td>
            <td class="num"><fmt:formatNumber value="${p.pension}" minFractionDigits="2"/></td>
            <td class="num"><fmt:formatNumber value="${p.otherDeductions}" minFractionDigits="2"/></td>
            <td class="num"><strong><fmt:formatNumber value="${p.net}" minFractionDigits="2"/></strong></td>
            <td><a href="${ctx}/payslips/view?id=${p.id}">Payslip</a></td>
        </tr>
    </c:forEach>
    <c:if test="${empty payslips}">
        <tr><td colspan="11" class="empty">No payslips yet. Click "Calculate" to generate them.</td></tr>
    </c:if>
    </tbody>
</table>

<p><a href="${ctx}/payruns/">&laquo; All pay runs</a></p>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
