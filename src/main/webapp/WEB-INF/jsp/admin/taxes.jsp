<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Settings" scope="request"/>
<c:set var="navSection" value="admin" scope="request"/>
<c:set var="adminTab" value="taxes"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<h1>Settings</h1>
<%@ include file="subnav.jspf" %>

<c:if test="${not empty errors}">
    <div class="message error" id="form-errors">
        <c:forEach items="${errors}" var="err"><div>${err.value}</div></c:forEach>
    </div>
</c:if>

<form method="post" action="${ctx}/admin/taxes" id="tax-form" class="form">
    <h2>Income tax brackets (monthly gross)</h2>
    <table class="grid" id="brackets-table">
        <thead><tr><th>From (USD)</th><th>To (USD, empty = no limit)</th><th>Rate (%)</th></tr></thead>
        <tbody>
        <c:forEach items="${brackets}" var="b" varStatus="st">
            <tr>
                <td><input type="text" name="lower" value="${b.lowerBound}" aria-label="Lower bound row ${st.count}"></td>
                <td><input type="text" name="upper" value="${b.upperBound}" aria-label="Upper bound row ${st.count}"></td>
                <td><input type="text" name="rate" value="${b.ratePercent.toPlainString()}" aria-label="Rate row ${st.count}"></td>
            </tr>
        </c:forEach>
        <tr class="new-row">
            <td><input type="text" name="lower" value="" aria-label="Lower bound new row"></td>
            <td><input type="text" name="upper" value="" aria-label="Upper bound new row"></td>
            <td><input type="text" name="rate" value="" aria-label="Rate new row"></td>
        </tr>
        </tbody>
    </table>

    <h2>Contributions</h2>
    <div class="field">
        <label for="socialRate">Social contribution (% of gross)</label>
        <input type="text" id="socialRate" name="socialRate" value="${socialRate}">
    </div>
    <div class="field">
        <label for="pensionRate">Pension, if opted in (% of gross)</label>
        <input type="text" id="pensionRate" name="pensionRate" value="${pensionRate}">
    </div>
    <div class="actions">
        <button type="submit" class="primary" id="save-taxes">Save settings</button>
    </div>
</form>

<h2>Deduction types</h2>
<table class="grid" id="deduction-types">
    <thead><tr><th>Code</th><th>Name</th><th>Description</th><th>Active</th></tr></thead>
    <tbody>
    <c:forEach items="${deductionTypes}" var="t">
        <tr><td>${t.code}</td><td><c:out value="${t.name}"/></td><td><c:out value="${t.description}"/></td><td>${t.active ? 'Yes' : 'No'}</td></tr>
    </c:forEach>
    </tbody>
</table>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
