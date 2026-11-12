<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Sign in" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<div class="login-box">
    <h1>Sign in</h1>
    <c:if test="${not empty loginError}">
        <div class="message error" id="login-error"><c:out value="${loginError}"/></div>
    </c:if>
    <form method="post" action="${ctx}/login" id="login-form" novalidate>
        <input type="hidden" name="next" value="<c:out value='${next}'/>">
        <div class="field">
            <label for="email">Email</label>
            <input type="email" id="email" name="email" value="<c:out value='${email}'/>" autofocus>
            <c:if test="${not empty errors.email}"><span class="field-error" id="email-error">${errors.email}</span></c:if>
        </div>
        <div class="field">
            <label for="password">Password</label>
            <input type="password" id="password" name="password">
            <c:if test="${not empty errors.password}"><span class="field-error" id="password-error">${errors.password}</span></c:if>
        </div>
        <div class="actions">
            <button type="submit" id="login-button" class="primary">Sign in</button>
        </div>
    </form>
    <p class="hint">Demo accounts: admin@acme.test, manager@acme.test, employee@acme.test</p>
</div>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
