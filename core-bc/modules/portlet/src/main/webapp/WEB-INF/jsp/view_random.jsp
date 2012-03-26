<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="init.jsp" %>

<h3>Slumpmässiga tal</h3>
<c:forEach items="${values}" var="value">
    <p>${value}</p>
</c:forEach>