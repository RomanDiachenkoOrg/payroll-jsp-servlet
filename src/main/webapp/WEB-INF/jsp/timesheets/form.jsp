<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="isNew" value="${empty timesheet.id}"/>
<c:set var="pageTitle" value="${isNew ? 'New timesheet' : 'Edit timesheet'}" scope="request"/>
<c:set var="navSection" value="timesheets" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<h1>${pageTitle}</h1>
<c:if test="${not empty errors}">
    <div class="message error" id="form-errors">
        <c:forEach items="${errors}" var="err"><div>${err.value}</div></c:forEach>
    </div>
</c:if>
<c:if test="${timesheet.status == 'REJECTED'}">
    <div class="message warning">Rejected: <c:out value="${timesheet.rejectReason}"/></div>
</c:if>

<form method="post" action="${ctx}/timesheets/save" id="timesheet-form" class="form" novalidate>
    <c:if test="${not isNew}"><input type="hidden" name="id" value="${timesheet.id}"></c:if>

    <c:if test="${isNew and me.admin}">
        <div class="field ${not empty errors.employeeId ? 'has-error' : ''}">
            <label for="employeeId">Employee</label>
            <select id="employeeId" name="employeeId">
                <option value="">-- select --</option>
                <c:forEach items="${employees}" var="e">
                    <option value="${e.id}" ${timesheet.employeeId == e.id ? 'selected' : ''}><c:out value="${e.lastName}, ${e.firstName}"/> (${e.employeeNo})</option>
                </c:forEach>
            </select>
        </div>
    </c:if>
    <div class="field ${not empty errors.weekStart ? 'has-error' : ''}">
        <label for="weekStart">Week starting (Monday)</label>
        <fmt:formatDate var="weekValue" value="${timesheet.weekStart}" pattern="yyyy-MM-dd"/>
        <c:choose>
            <c:when test="${isNew}">
                <input type="text" id="weekStart" name="weekStart" class="date monday" value="${weekValue}">
            </c:when>
            <c:otherwise>
                <input type="text" id="weekStart" value="${weekValue}" readonly>
            </c:otherwise>
        </c:choose>
    </div>

    <table class="grid hours" id="hours-table">
        <thead>
        <tr>
            <c:forEach items="${timesheet.entries}" var="e">
                <th><fmt:formatDate value="${e.workDate}" pattern="EEE"/><br><span class="sub"><fmt:formatDate value="${e.workDate}" pattern="MM/dd"/></span></th>
            </c:forEach>
            <th>Total</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <c:forEach items="${timesheet.entries}" var="e" varStatus="st">
                <c:set var="hk" value="hours${st.index}"/>
                <td class="${not empty errors[hk] ? 'has-error' : ''}">
                    <label for="hours${st.index}" class="sr-only">Hours on <fmt:formatDate value="${e.workDate}" pattern="EEEE"/></label>
                    <input type="text" id="hours${st.index}" name="hours${st.index}" class="hours-input" size="4"
                           value="<fmt:formatNumber value='${e.hours}' maxFractionDigits='2' groupingUsed='false'/>">
                </td>
            </c:forEach>
            <td class="num"><strong id="total-hours"><fmt:formatNumber value="${timesheet.totalHours}" maxFractionDigits="2"/></strong></td>
        </tr>
        </tbody>
    </table>
    <p class="hint">Enter 0 to 24 hours per day. Hours above 40 in a week are paid as overtime (1.5x).</p>

    <div class="actions">
        <button type="submit" name="op" value="save" id="save-draft">Save draft</button>
        <button type="submit" name="op" value="submit" class="primary" id="submit-timesheet">Submit for approval</button>
        <a href="${ctx}/timesheets/">Cancel</a>
    </div>
</form>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
