<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Payroll cost by department" scope="request"/>
<c:set var="navSection" value="reports" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<h1>Payroll cost by department</h1>

<form method="get" action="${ctx}/reports/cost" class="filter-bar" id="report-filter">
    <label for="from">Pay date from</label>
    <input type="text" id="from" name="from" class="date" value="${from}">
    <label for="to">to</label>
    <input type="text" id="to" name="to" class="date" value="${to}">
    <button type="submit" id="run-report">Run report</button>
    <a href="${ctx}/reports/cost.csv?from=${from}&amp;to=${to}" class="button" id="export-report">Export CSV</a>
</form>

<table class="grid" id="report-table">
    <thead>
    <tr><th>Department</th><th class="num">Headcount</th><th class="num">Gross</th><th class="num">Income tax</th>
        <th class="num">Social</th><th class="num">Pension</th><th class="num">Net</th></tr>
    </thead>
    <tbody>
    <c:forEach items="${rows}" var="r">
        <tr>
            <td><c:out value="${r.departmentName}"/></td>
            <td class="num">${r.headcount}</td>
            <td class="num"><fmt:formatNumber value="${r.gross}" type="currency" currencyCode="USD"/></td>
            <td class="num"><fmt:formatNumber value="${r.tax}" type="currency" currencyCode="USD"/></td>
            <td class="num"><fmt:formatNumber value="${r.social}" type="currency" currencyCode="USD"/></td>
            <td class="num"><fmt:formatNumber value="${r.pension}" type="currency" currencyCode="USD"/></td>
            <td class="num"><fmt:formatNumber value="${r.net}" type="currency" currencyCode="USD"/></td>
        </tr>
    </c:forEach>
    <c:if test="${empty rows}">
        <tr><td colspan="7" class="empty">No approved or paid pay runs in this period.</td></tr>
    </c:if>
    </tbody>
    <c:if test="${not empty rows}">
    <tfoot>
    <tr class="total">
        <th>Total</th><td></td>
        <td class="num"><fmt:formatNumber value="${total.gross}" type="currency" currencyCode="USD"/></td>
        <td class="num"><fmt:formatNumber value="${total.tax}" type="currency" currencyCode="USD"/></td>
        <td class="num"><fmt:formatNumber value="${total.social}" type="currency" currencyCode="USD"/></td>
        <td class="num"><fmt:formatNumber value="${total.pension}" type="currency" currencyCode="USD"/></td>
        <td class="num"><fmt:formatNumber value="${total.net}" type="currency" currencyCode="USD"/></td>
    </tr>
    </tfoot>
    </c:if>
</table>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
