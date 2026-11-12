<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Pay runs" scope="request"/>
<c:set var="navSection" value="payruns" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<h1>Pay runs</h1>

<table class="grid" id="payruns-table">
    <thead>
    <tr><th>Period</th><th>Pay date</th><th>Status</th><th class="num">Payslips</th><th class="num">Gross</th><th class="num">Net</th><th></th></tr>
    </thead>
    <tbody>
    <c:forEach items="${payRuns}" var="r">
        <tr>
            <td><fmt:formatDate value="${r.periodStart}" pattern="yyyy-MM-dd"/> &ndash; <fmt:formatDate value="${r.periodEnd}" pattern="yyyy-MM-dd"/></td>
            <td><fmt:formatDate value="${r.payDate}" pattern="yyyy-MM-dd"/></td>
            <td><span class="status status-${r.status}">${r.status}</span></td>
            <td class="num">${r.payslipCount}</td>
            <td class="num"><fmt:formatNumber value="${r.totalGross}" type="currency" currencyCode="USD"/></td>
            <td class="num"><fmt:formatNumber value="${r.totalNet}" type="currency" currencyCode="USD"/></td>
            <td><a href="${ctx}/payruns/view?id=${r.id}">Open</a></td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<h2>Create pay run</h2>
<form method="post" action="${ctx}/payruns/create" id="payrun-form" class="form compact">
    <div class="field">
        <label for="periodStart">Period start</label>
        <input type="text" id="periodStart" name="periodStart" class="date" value="${suggestStart}">
    </div>
    <div class="field">
        <label for="periodEnd">Period end</label>
        <input type="text" id="periodEnd" name="periodEnd" class="date" value="${suggestEnd}">
    </div>
    <div class="field">
        <label for="payDate">Pay date</label>
        <input type="text" id="payDate" name="payDate" class="date" value="${suggestEnd}">
    </div>
    <button type="submit" class="primary" id="create-payrun">Create</button>
</form>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
