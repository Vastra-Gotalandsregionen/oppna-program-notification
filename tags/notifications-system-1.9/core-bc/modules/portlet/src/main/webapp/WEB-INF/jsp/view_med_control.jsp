<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="init.jsp" %>

<div class="notification-wrap">
	<h3>MedControl</h3>
	
	<ul class="notifications-list med-control">
	    <c:forEach items="${deviationCases}" var="deviationCase" varStatus="casesIteratorStatus">
		    	<c:set var="listItemCssClass" value="" scope="page" />
		    	<c:choose>
		    		<c:when test="${casesIteratorStatus.first}">
		    			<c:set var="listItemCssClass" value="first" scope="page" />
		    		</c:when>
		    		<c:when test="${casesIteratorStatus.last}">
		    			<c:set var="listItemCssClass" value="last" scope="page" />
		    		</c:when>
		    	</c:choose>
		    	
		    	<li class="${listItemCssClass}">
		    		<%--<a href="${deviationCase.url}" target="_blank">${document.fileName}</a>--%>
                    <a href="${deviationCase.url}" target="_blank" class="clearfix">
	    			    <span class="notification-summary notification-med-control-summary">${deviationCase.caseNumber} - ${deviationCase.description}</span>
                    </a>
		    	</li>
	    </c:forEach>
	</ul>
</div>