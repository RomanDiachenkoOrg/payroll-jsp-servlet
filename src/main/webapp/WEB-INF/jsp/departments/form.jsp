<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="${empty department.id ? 'New department' : 'Edit department'}" scope="request"/>
<c:set var="navSection" value="departments" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<h1>${pageTitle}</h1>

<form method="post" action="${ctx}/departments/save" id="department-form" class="form" novalidate>
    <c:if test="${not empty department.id}"><input type="hidden" name="id" value="${department.id}"></c:if>
    <div class="field ${not empty errors.code ? 'has-error' : ''}">
        <label for="code">Code *</label>
        <input type="text" id="code" name="code" maxlength="10" value="<c:out value='${department.code}'/>">
        <c:if test="${not empty errors.code}"><span class="field-error">${errors.code}</span></c:if>
    </div>
    <div class="field ${not empty errors.name ? 'has-error' : ''}">
        <label for="name">Name *</label>
        <input type="text" id="name" name="name" maxlength="100" value="<c:out value='${department.name}'/>">
        <c:if test="${not empty errors.name}"><span class="field-error">${errors.name}</span></c:if>
    </div>
    <div class="field">
        <label for="costCenter">Cost center</label>
        <input type="text" id="costCenter" name="costCenter" maxlength="20" value="<c:out value='${department.costCenter}'/>">
    </div>
    <div class="actions">
        <button type="submit" class="primary" id="save-department">Save</button>
        <a href="${ctx}/departments/">Cancel</a>
    </div>
</form>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
