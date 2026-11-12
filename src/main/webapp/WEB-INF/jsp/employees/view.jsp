<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="${employee.fullName}" scope="request"/>
<c:set var="navSection" value="${sessionScope.currentUser.employee ? 'profile' : 'employees'}" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<div class="page-head">
    <h1><c:out value="${employee.fullName}"/>
        <span class="status ${employee.active ? 'status-active' : 'status-inactive'}">${employee.active ? 'Active' : 'Inactive'}</span>
    </h1>
    <c:if test="${me.admin}">
        <div class="buttons">
            <a href="${ctx}/employees/edit?id=${employee.id}" class="button" id="edit-employee">Edit</a>
            <c:choose>
                <c:when test="${employee.active}">
                    <form method="post" action="${ctx}/employees/deactivate" class="inline confirm"
                          data-confirm="Deactivate ${employee.fullName}? The employee will no longer be included in pay runs.">
                        <input type="hidden" name="id" value="${employee.id}">
                        <button type="submit" class="danger" id="deactivate-employee">Deactivate</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${ctx}/employees/activate" class="inline">
                        <input type="hidden" name="id" value="${employee.id}">
                        <button type="submit" id="activate-employee">Reactivate</button>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>
</div>

<div id="tabs">
    <ul>
        <li><a href="#tab-profile">Profile</a></li>
        <li><a href="#tab-salary">Salary history</a></li>
        <li><a href="#tab-payslips">Payslips</a></li>
    </ul>

    <div id="tab-profile">
        <table class="details" id="employee-details">
            <tr><th>Employee no.</th><td>${employee.employeeNo}</td></tr>
            <tr><th>Email</th><td><c:out value="${employee.email}"/></td></tr>
            <tr><th>Phone</th><td><c:out value="${employee.phone}"/></td></tr>
            <tr><th>Department</th><td><c:out value="${employee.departmentName}"/></td></tr>
            <tr><th>Job title</th><td><c:out value="${employee.jobTitle}"/></td></tr>
            <tr><th>Hire date</th><td><fmt:formatDate value="${employee.hireDate}" pattern="yyyy-MM-dd"/></td></tr>
            <tr><th>Pay type</th><td>${employee.payType.label}</td></tr>
            <c:choose>
                <c:when test="${employee.hourly}">
                    <tr><th>Hourly rate</th><td><fmt:formatNumber value="${employee.hourlyRate}" type="currency" currencyCode="USD"/></td></tr>
                </c:when>
                <c:otherwise>
                    <tr><th>Annual salary</th><td><fmt:formatNumber value="${employee.annualSalary}" type="currency" currencyCode="USD"/>
                        (<fmt:formatNumber value="${employee.monthlySalary}" type="currency" currencyCode="USD"/> / month)</td></tr>
                </c:otherwise>
            </c:choose>
            <tr><th>Pension plan</th><td>${employee.pensionOptIn ? 'Yes' : 'No'}</td></tr>
            <tr><th>Recurring deductions</th><td>
                <c:forEach items="${deductions}" var="d"><c:out value="${d.key}"/>: <fmt:formatNumber value="${d.value}" type="currency" currencyCode="USD"/><br></c:forEach>
                <c:if test="${empty deductions}">None</c:if>
            </td></tr>
        </table>
    </div>

    <div id="tab-salary">
        <table class="grid" id="salary-history">
            <thead><tr><th>Effective</th><th class="num">Annual salary</th><th class="num">Hourly rate</th><th>Reason</th><th>By</th></tr></thead>
            <tbody>
            <c:forEach items="${salaryHistory}" var="h">
                <tr>
                    <td><fmt:formatDate value="${h.effectiveDate}" pattern="yyyy-MM-dd"/></td>
                    <td class="num"><fmt:formatNumber value="${h.annualSalary}" type="currency" currencyCode="USD"/></td>
                    <td class="num"><fmt:formatNumber value="${h.hourlyRate}" type="currency" currencyCode="USD"/></td>
                    <td><c:out value="${h.reason}"/></td>
                    <td><c:out value="${h.createdBy}"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <c:if test="${me.admin and employee.active}">
            <h3>Add raise</h3>
            <form method="post" action="${ctx}/employees/raise" id="raise-form" class="form compact" novalidate>
                <input type="hidden" name="id" value="${employee.id}">
                <div class="field ${not empty raiseErrors.amount ? 'has-error' : ''}">
                    <label for="amount">New ${employee.hourly ? 'hourly rate' : 'annual salary'} (USD)</label>
                    <input type="text" id="amount" name="amount" value="<c:out value='${raiseAmount}'/>">
                    <c:if test="${not empty raiseErrors.amount}"><span class="field-error">${raiseErrors.amount}</span></c:if>
                </div>
                <div class="field ${not empty raiseErrors.effectiveDate ? 'has-error' : ''}">
                    <label for="effectiveDate">Effective date</label>
                    <input type="text" id="effectiveDate" name="effectiveDate" class="date" value="<c:out value='${empty raiseDate ? today : raiseDate}'/>">
                    <c:if test="${not empty raiseErrors.effectiveDate}"><span class="field-error">${raiseErrors.effectiveDate}</span></c:if>
                </div>
                <div class="field ${not empty raiseErrors.reason ? 'has-error' : ''}">
                    <label for="reason">Reason</label>
                    <input type="text" id="reason" name="reason" maxlength="200" value="<c:out value='${raiseReason}'/>">
                    <c:if test="${not empty raiseErrors.reason}"><span class="field-error">${raiseErrors.reason}</span></c:if>
                </div>
                <button type="submit" id="add-raise">Add raise</button>
            </form>
        </c:if>
    </div>

    <div id="tab-payslips">
        <table class="grid" id="employee-payslips">
            <thead><tr><th>Period</th><th>Pay date</th><th class="num">Gross</th><th class="num">Deductions</th><th class="num">Net</th><th></th></tr></thead>
            <tbody>
            <c:forEach items="${payslips}" var="p">
                <tr>
                    <td><fmt:formatDate value="${p.periodStart}" pattern="MMMM yyyy"/></td>
                    <td><fmt:formatDate value="${p.payDate}" pattern="yyyy-MM-dd"/></td>
                    <td class="num"><fmt:formatNumber value="${p.gross}" type="currency" currencyCode="USD"/></td>
                    <td class="num"><fmt:formatNumber value="${p.totalDeductions}" type="currency" currencyCode="USD"/></td>
                    <td class="num"><fmt:formatNumber value="${p.net}" type="currency" currencyCode="USD"/></td>
                    <td><a href="${ctx}/payslips/view?id=${p.id}">View</a></td>
                </tr>
            </c:forEach>
            <c:if test="${empty payslips}"><tr><td colspan="6" class="empty">No payslips.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
