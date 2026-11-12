<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="code" value="${requestScope['javax.servlet.error.status_code']}"/>
<c:set var="pageTitle" value="Error ${code}" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>
<h1>
    <c:choose>
        <c:when test="${code == 403}">Access denied</c:when>
        <c:when test="${code == 404}">Page not found</c:when>
        <c:otherwise>Something went wrong</c:otherwise>
    </c:choose>
</h1>
<p>
    <c:choose>
        <c:when test="${code == 403}">You do not have permission to view this page.</c:when>
        <c:when test="${code == 404}">The page or record you requested does not exist.</c:when>
        <c:otherwise>An unexpected error occurred. The problem has been logged.</c:otherwise>
    </c:choose>
</p>
<p><a href="${pageContext.request.contextPath}/dashboard">Back to the dashboard</a></p>
<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
