<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="init.jsp" %>

<div class="notification-wrap">
	<h3>Slumpmässiga tal</h3>
	
	<ul class="notifications-list usd-issues">
	    <c:forEach items="${values}" var="value" varStatus="iteratorStatus">
	    	<c:set var="listItemCssClass" value="" scope="page" />
	    	<c:choose>
	    		<c:when test="${iteratorStatus.first}">
	    			<c:set var="listItemCssClass" value="first" scope="page" />
	    		</c:when>
	    		<c:when test="${iteratorStatus.last}">
	    			<c:set var="listItemCssClass" value="last" scope="page" />
	    		</c:when>
	    	</c:choose>
	    
	    	<li class=""${listItemCssClass}>
	    		<span>${value}</span>
	    	</li>
	    </c:forEach>
	</ul>
</div>