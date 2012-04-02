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
                <c:choose>
                    <c:when test="${invoice.type eq 'O'}">
                        <img class="rd-${invoice.type}" width="12px" height="12px" src="/regionportalen-theme/images/red22x22.png" alt="overdue">
                    </c:when>
                    <c:when test="${invoice.type eq 'U'}">
                        <img class="rd-${invoice.type}" width="12px" height="12px" src="/regionportalen-theme/images/yellow22x22.png" alt="overdue">
                    </c:when>
                </c:choose>
	    		<a href="/group/vgregion/pub-ekonomi" target="_blank">${invoice.supplierName}</a>
	    	</li>
	    </c:forEach>
	</ul>

</div>