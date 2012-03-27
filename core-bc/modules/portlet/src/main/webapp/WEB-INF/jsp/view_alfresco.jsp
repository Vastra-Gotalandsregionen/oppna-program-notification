<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="init.jsp" %>

<div class="notification-wrap">
	<h3>Alfresco</h3>
	
	<ul class="notifications-list alfresco">
	    <c:forEach items="${sites}" var="site" varStatus="sitesIteratorStatus">
	    	<c:forEach items="${site.recentModifiedDocuments}" var="document" varStatus="documentsIteratorStatus">

		    	<c:set var="listItemCssClass" value="" scope="page" />
		    	<c:choose>
		    		<c:when test="${sitesIteratorStatus.first and documentsIteratorStatus.first}">
		    			<c:set var="listItemCssClass" value="first" scope="page" />
		    		</c:when>
		    		<c:when test="${sitesIteratorStatus.last and documentsIteratorStatus.last}">
		    			<c:set var="listItemCssClass" value="last" scope="last" />
		    		</c:when>
		    	</c:choose>
		    	
		    	<li class=""${listItemCssClass}>
		    		<a href="${site.shareUrl}">${document.fileName}</a>
		    	</li>
	    	
	    	</c:forEach>
	    </c:forEach>
	</ul>
</div>