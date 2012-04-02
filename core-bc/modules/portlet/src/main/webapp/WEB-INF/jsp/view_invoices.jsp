<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="init.jsp" %>

<portlet:resourceURL var="bopsIdUrl" id="lookupBopsId" />


<div class="notification-wrap">
	<h3>Fakturor</h3>
	
	<ul class="notifications-list invoices">
	    <c:forEach items="${invoices}" var="invoice" varStatus="iteratorStatus">
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
	    			<a href="/group/vgregion/pub-ekonomi" target="_blank" class="clearfix">
		    			<span class="notification-types notification-usd-types">
		    				<c:set var="notificationTypeCssClass" value="" scope="page" />
		    				<c:choose>
		    					<c:when test="${invoice.type == 'O'}">
		    						<c:set var="notificationTypeCssClass" value="notification-type-1" scope="page" />
		    					</c:when>
		    					<c:when test="${invoice.type == 'U'}">
		    						<c:set var="notificationTypeCssClass" value="notification-type-2" scope="page" />
		    					</c:when>
		    					<c:otherwise>
		    						<c:set var="notificationTypeCssClass" value="" scope="page" />
		    					</c:otherwise>
		    				</c:choose>
		    				<span class="notification-type ${notificationTypeCssClass}">
		    					${invoice.type}
		    				</span>
	    				</span>
		    			<span class="notification-summary">${invoice.supplierName}</span>
	    			</a>
	    	</li>
	    </c:forEach>
	</ul>

</div>