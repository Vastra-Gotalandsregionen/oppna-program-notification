<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="init.jsp" %>

<style type="text/css">
    .social-request-list-item .full-name {
        padding-right: 20px;
    }

    .social-request-list-item .confirm-request {
        padding-right: 10px;
    }
</style>

<c:set var="userUrlPrefix" value="/group/vgregion/social/-/user/"/>

<div class="notification-wrap">
	<h3>Vänförfrågningar</h3>
	
	<ul class="notifications-list social-requests">
	    <c:forEach items="${socialRequests}" var="socialRequest" varStatus="iteratorStatus">
	    	<c:set var="listItemCssClass" value="" scope="page" />
            <portlet:resourceURL var="confirmRequest" id="confirmRequest">
                <portlet:param name="requestId" value="${socialRequest.key.requestId}"/>
            </portlet:resourceURL>
            <portlet:resourceURL var="rejectRequest" id="rejectRequest">
                <portlet:param name="requestId" value="${socialRequest.key.requestId}"/>
            </portlet:resourceURL>

	    	<c:choose>
	    		<c:when test="${iteratorStatus.first}">
	    			<c:set var="listItemCssClass" value="first" scope="page" />
	    		</c:when>
	    		<c:when test="${iteratorStatus.last}">
	    			<c:set var="listItemCssClass" value="last" scope="page" />
	    		</c:when>
	    	</c:choose>
	    
	    	<li class="social-request-list-item ${listItemCssClass}">
                <span class="full-name">
                    <a href="${userUrlPrefix}${socialRequest.value.screenName}">
                        <c:out value="${socialRequest.value.fullName}"/>
                    </a>
                </span>
                <span class="confirm-request"><a class="request-choice" href="${confirmRequest}">Acceptera</a></span>
                <span class="reject-request"><a class="request-choice" href="${rejectRequest}">Ignorera</a></span>
	    	</li>
	    </c:forEach>
	</ul>

</div>

<script type="text/javascript" src="<%= request.getContextPath() %>/js/view-social-requests.js">

</script>