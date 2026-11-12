<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Payslip" scope="request"/>
<jsp:include page="/WEB-INF/jsp/include/header.jsp"/>

<div class="page-head">
    <h1>Payslip &ndash; <fmt:formatDate value="${payslip.periodStart}" pattern="MMMM yyyy"/></h1>
    <div class="buttons">
        <a href="${ctx}/payslips/print?id=${payslip.id}" class="button" target="_blank" id="print-payslip">Print</a>
        <a href="${ctx}/payslips/csv?id=${payslip.id}" class="button" id="csv-payslip">Download CSV</a>
    </div>
</div>

<%@ include file="breakdown.jspf" %>

<p>
    <c:choose>
        <c:when test="${me.admin}"><a href="${ctx}/payruns/view?id=${payslip.payRunId}">&laquo; Back to pay run</a></c:when>
        <c:otherwise><a href="${ctx}/employees/view?id=${payslip.employeeId}#tab-payslips">&laquo; Back</a></c:otherwise>
    </c:choose>
</p>

<jsp:include page="/WEB-INF/jsp/include/footer.jsp"/>
