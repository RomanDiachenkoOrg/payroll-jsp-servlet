<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Departments" scope="request"/>
<c:set var="navSection" value="departments" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<div class="page-head">
    <h1>Departments</h1>
    <a href="${ctx}/departments/new" class="button primary" id="new-department">New department</a>
</div>

<table class="grid" id="departments-table">
    <thead><tr><th>Code</th><th>Name</th><th>Cost center</th><th class="num">Active employees</th><th class="num">Total</th><th></th></tr></thead>
    <tbody>
    <c:forEach items="${departments}" var="d">
        <tr>
            <td>${d.code}</td>
            <td><c:out value="${d.name}"/></td>
            <td><c:out value="${d.costCenter}"/></td>
            <td class="num">${d.activeEmployees}</td>
            <td class="num">${d.totalEmployees}</td>
            <td class="row-actions">
                <a href="${ctx}/departments/edit?id=${d.id}">Edit</a>
                <form method="post" action="${ctx}/departments/delete" class="inline confirm"
                      data-confirm="Delete department <c:out value='${d.name}'/>?">
                    <input type="hidden" name="id" value="${d.id}">
                    <button type="submit" class="link danger" id="delete-department-${d.id}">Delete</button>
                </form>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
