<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Dashboard" scope="request"/>
<c:set var="navSection" value="dashboard" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<h1>Dashboard</h1>

<c:choose>
<c:when test="${me.employee}">
    <div class="cards">
        <div class="card" id="card-next-pay-date">
            <div class="card-label">Next pay date</div>
            <div class="card-value"><fmt:formatDate value="${nextPayDate}" pattern="MMM d, yyyy"/></div>
        </div>
        <div class="card">
            <div class="card-label">My timesheets</div>
            <div class="card-value">${fn:length(myTimesheets)}</div>
        </div>
    </div>
    <h2>Recent payslips</h2>
    <table class="grid" id="recent-payslips">
        <thead><tr><th>Period</th><th>Pay date</th><th class="num">Gross</th><th class="num">Net</th><th></th></tr></thead>
        <tbody>
        <c:forEach items="${payslips}" var="p">
            <tr>
                <td><fmt:formatDate value="${p.periodStart}" pattern="MMMM yyyy"/></td>
                <td><fmt:formatDate value="${p.payDate}" pattern="yyyy-MM-dd"/></td>
                <td class="num"><fmt:formatNumber value="${p.gross}" type="currency" currencyCode="USD"/></td>
                <td class="num"><fmt:formatNumber value="${p.net}" type="currency" currencyCode="USD"/></td>
                <td><a href="${ctx}/payslips/view?id=${p.id}">View</a></td>
            </tr>
        </c:forEach>
        <c:if test="${empty payslips}"><tr><td colspan="5" class="empty">No payslips yet.</td></tr></c:if>
        </tbody>
    </table>
    <p><a class="button" href="${ctx}/timesheets/new">Enter this week's timesheet</a></p>
</c:when>
<c:otherwise>
    <div class="cards">
        <div class="card" id="card-headcount">
            <div class="card-label">Active headcount</div>
            <div class="card-value">${headcount}</div>
        </div>
        <div class="card" id="card-last-payroll">
            <div class="card-label">Last payroll (gross)</div>
            <div class="card-value">
                <c:choose>
                    <c:when test="${not empty lastRun}"><fmt:formatNumber value="${lastRun.totalGross}" type="currency" currencyCode="USD"/></c:when>
                    <c:otherwise>-</c:otherwise>
                </c:choose>
            </div>
            <c:if test="${not empty lastRun}"><div class="card-sub">${lastRun.periodLabel}</div></c:if>
        </div>
        <div class="card" id="card-pending-timesheets">
            <div class="card-label">Pending timesheets</div>
            <div class="card-value"><a href="${ctx}/timesheets/?status=SUBMITTED">${pendingTimesheets}</a></div>
        </div>
        <div class="card" id="card-next-pay-date">
            <div class="card-label">Next pay date</div>
            <div class="card-value"><fmt:formatDate value="${nextPayDate}" pattern="MMM d, yyyy"/></div>
        </div>
    </div>

    <div class="two-col">
        <div>
            <h2>Last payroll by department</h2>
            <table class="chart" id="dept-chart">
                <c:forEach items="${deptCosts}" var="d">
                    <tr>
                        <th><c:out value="${d.departmentName}"/></th>
                        <td><div class="bar" style="width: ${d.gross * 100 / deptMax}%"></div></td>
                        <td class="num"><fmt:formatNumber value="${d.gross}" type="currency" currencyCode="USD" maxFractionDigits="0"/></td>
                    </tr>
                </c:forEach>
            </table>
        </div>
        <div>
            <h2>Recent pay runs</h2>
            <table class="grid" id="recent-runs">
                <thead><tr><th>Period</th><th>Pay date</th><th class="num">Employees</th><th class="num">Net total</th></tr></thead>
                <tbody>
                <c:forEach items="${recentRuns}" var="r">
                    <tr>
                        <td>${r.periodLabel}</td>
                        <td><fmt:formatDate value="${r.payDate}" pattern="yyyy-MM-dd"/></td>
                        <td class="num">${r.payslipCount}</td>
                        <td class="num"><fmt:formatNumber value="${r.totalNet}" type="currency" currencyCode="USD"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
