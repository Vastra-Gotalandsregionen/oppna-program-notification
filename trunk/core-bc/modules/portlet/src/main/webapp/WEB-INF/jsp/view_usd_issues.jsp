<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="init.jsp" %>

<portlet:resourceURL var="bopsIdUrl" id="lookupBopsId" />

<div class="notification-wrap">
	<h3>&Auml;renden i Navet</h3>
	
	<ul class="notifications-list usd-issues">
	    <c:forEach items="${usdIssues}" var="usdIssue" varStatus="iteratorStatus">
	    	<c:set var="listItemCssClass" value="" scope="page" />
	    	<c:choose>
	    		<c:when test="${iteratorStatus.first}">
	    			<c:set var="listItemCssClass" value="first" scope="page" />
	    		</c:when>
	    		<c:when test="${iteratorStatus.last}">
	    			<c:set var="listItemCssClass" value="last" scope="page" />
	    		</c:when>
	    	</c:choose>
	    
	    	<li class="${listItemCssClass}">
	    		<a href="${usdIssue.url}" target="_blank" class="clearfix">
	    			<span class="notification-types notification-usd-types">
	    				<c:set var="notificationTypeCssClass" value="" scope="page" />
	    				<c:choose>
	    					<c:when test="${usdIssue.type == 'A'}">
	    					<c:set var="notificationTypeCssClass" value="notification-usd-type-warning" scope="page" />
	    					</c:when>
	    					<c:otherwise>
	    						<c:set var="notificationTypeCssClass" value="" scope="page" />
	    					</c:otherwise>
	    				</c:choose>
	    				<span class="notification-type notification-usd-type ${notificationTypeCssClass}">
	    					${usdIssue.type}
	    				</span>
    				</span>
	    			<span class="notification-summary notification-usd-summary">${usdIssue.summary}</span>
    			</a>
	    	</li>
	    </c:forEach>
	</ul>

</div>

<script type="text/javascript" src="<%= request.getContextPath() %>/js/view-usd-issues.js">

</script>