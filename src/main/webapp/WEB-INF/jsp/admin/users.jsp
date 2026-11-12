<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Users" scope="request"/>
<c:set var="navSection" value="admin" scope="request"/>
<c:set var="adminTab" value="users"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<h1>Settings</h1>
<%@ include file="subnav.jspf" %>

<table class="grid" id="users-table">
    <thead><tr><th>Email</th><th>Name</th><th>Role</th><th>Last login</th><th>Status</th><th></th></tr></thead>
    <tbody>
    <c:forEach items="${users}" var="u">
        <tr class="${u.active ? '' : 'inactive'}">
            <td><c:out value="${u.email}"/></td>
            <td><c:out value="${u.fullName}"/></td>
            <td><span class="role role-${u.role}">${u.role}</span></td>
            <td><fmt:formatDate value="${u.lastLogin}" pattern="yyyy-MM-dd HH:mm"/></td>
            <td>${u.active ? 'Enabled' : 'Disabled'}</td>
            <td>
                <c:if test="${u.id != me.id}">
                    <form method="post" action="${ctx}/admin/users/toggle" class="inline confirm"
                          data-confirm="${u.active ? 'Disable' : 'Enable'} user <c:out value='${u.email}'/>?">
                        <input type="hidden" name="id" value="${u.id}">
                        <input type="hidden" name="active" value="${not u.active}">
                        <button type="submit" class="link">${u.active ? 'Disable' : 'Enable'}</button>
                    </form>
                </c:if>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
