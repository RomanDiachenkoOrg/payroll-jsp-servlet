<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Payslip ${payslip.employeeNo}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/print.css">
</head>
<body onload="window.print()">
<div class="print-head">
    <h1><c:out value="${companyName}"/></h1>
    <h2>Payslip &ndash; <fmt:formatDate value="${payslip.periodStart}" pattern="MMMM yyyy"/></h2>
</div>
<%@ include file="breakdown.jspf" %>
<p class="small">This payslip was generated electronically and is valid without a signature. All amounts in USD.</p>
</body>
</html>
