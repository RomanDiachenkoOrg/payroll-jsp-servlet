<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="isNew" value="${empty employee.id}"/>
<c:set var="pageTitle" value="${isNew ? 'New employee' : 'Edit employee'}" scope="request"/>
<c:set var="navSection" value="employees" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<h1>${pageTitle}</h1>
<c:if test="${not empty errors}">
    <div class="message error" id="form-errors">Please correct the highlighted fields.</div>
</c:if>

<fmt:formatDate var="hireDateValue" value="${employee.hireDate}" pattern="yyyy-MM-dd"/>
<c:if test="${not empty hireDateText}"><c:set var="hireDateValue" value="${hireDateText}"/></c:if>

<form method="post" action="${ctx}/employees/save" id="employee-form" class="form" novalidate>
    <c:if test="${not isNew}"><input type="hidden" name="id" value="${employee.id}"></c:if>

    <fieldset>
        <legend>Personal details</legend>
        <div class="field ${not empty errors.firstName ? 'has-error' : ''}">
            <label for="firstName">First name *</label>
            <input type="text" id="firstName" name="firstName" maxlength="60" value="<c:out value='${employee.firstName}'/>">
            <c:if test="${not empty errors.firstName}"><span class="field-error">${errors.firstName}</span></c:if>
        </div>
        <div class="field ${not empty errors.lastName ? 'has-error' : ''}">
            <label for="lastName">Last name *</label>
            <input type="text" id="lastName" name="lastName" maxlength="60" value="<c:out value='${employee.lastName}'/>">
            <c:if test="${not empty errors.lastName}"><span class="field-error">${errors.lastName}</span></c:if>
        </div>
        <div class="field ${not empty errors.email ? 'has-error' : ''}">
            <label for="email">Email *</label>
            <input type="email" id="email" name="email" maxlength="120" value="<c:out value='${employee.email}'/>">
            <c:if test="${not empty errors.email}"><span class="field-error">${errors.email}</span></c:if>
        </div>
        <div class="field">
            <label for="phone">Phone</label>
            <input type="text" id="phone" name="phone" maxlength="30" value="<c:out value='${employee.phone}'/>">
        </div>
    </fieldset>

    <fieldset>
        <legend>Employment</legend>
        <div class="field ${not empty errors.departmentId ? 'has-error' : ''}">
            <label for="departmentId">Department *</label>
            <select id="departmentId" name="departmentId">
                <option value="">-- select --</option>
                <c:forEach items="${departments}" var="d">
                    <option value="${d.id}" ${employee.departmentId == d.id ? 'selected' : ''}><c:out value="${d.name}"/></option>
                </c:forEach>
            </select>
            <c:if test="${not empty errors.departmentId}"><span class="field-error">${errors.departmentId}</span></c:if>
        </div>
        <div class="field">
            <label for="jobTitle">Job title</label>
            <input type="text" id="jobTitle" name="jobTitle" maxlength="100" value="<c:out value='${employee.jobTitle}'/>">
        </div>
        <div class="field ${not empty errors.hireDate ? 'has-error' : ''}">
            <label for="hireDate">Hire date *</label>
            <input type="text" id="hireDate" name="hireDate" class="date" value="<c:out value='${hireDateValue}'/>" placeholder="yyyy-mm-dd">
            <c:if test="${not empty errors.hireDate}"><span class="field-error">${errors.hireDate}</span></c:if>
        </div>
    </fieldset>

    <fieldset>
        <legend>Pay</legend>
        <div class="field ${not empty errors.payType ? 'has-error' : ''}">
            <span class="label">Pay type *</span>
            <c:forEach items="${payTypes}" var="pt">
                <label class="radio"><input type="radio" name="payType" value="${pt}" id="payType-${pt}" ${employee.payType == pt ? 'checked' : ''}> ${pt.label}</label>
            </c:forEach>
            <c:if test="${not empty errors.payType}"><span class="field-error">${errors.payType}</span></c:if>
        </div>
        <div class="field pay-salaried ${not empty errors.annualSalary ? 'has-error' : ''}">
            <label for="annualSalary">Annual salary (USD) *</label>
            <input type="text" id="annualSalary" name="annualSalary" value="${employee.annualSalary}">
            <c:if test="${not empty errors.annualSalary}"><span class="field-error">${errors.annualSalary}</span></c:if>
        </div>
        <div class="field pay-hourly ${not empty errors.hourlyRate ? 'has-error' : ''}">
            <label for="hourlyRate">Hourly rate (USD) *</label>
            <input type="text" id="hourlyRate" name="hourlyRate" value="${employee.hourlyRate}">
            <c:if test="${not empty errors.hourlyRate}"><span class="field-error">${errors.hourlyRate}</span></c:if>
        </div>
        <div class="field">
            <label class="checkbox"><input type="checkbox" id="pensionOptIn" name="pensionOptIn" ${employee.pensionOptIn ? 'checked' : ''}> Pension plan (3% of gross)</label>
        </div>
    </fieldset>

    <div class="actions">
        <button type="submit" class="primary" id="save-employee">Save</button>
        <a href="${ctx}${isNew ? '/employees/' : '/employees/view?id='}${isNew ? '' : employee.id}" id="cancel">Cancel</a>
    </div>
</form>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
