<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="init.jsp" %>

<portlet:resourceURL var="bopsIdUrl" id="lookupBopsId" />


<div class="notification-wrap">
	<h3>USD-ärenden</h3>
	
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
	    		<a href="${usdIssue.url}" target="_blank">${usdIssue.type} - ${usdIssue.summary}</a>
	    	</li>
	    </c:forEach>
	</ul>

</div>

<script type="text/javascript">
    AUI().ready('io', function(A) {
        A.all('.usd-issues a').on('click', function(e) {
            e.halt();
            var url = e.target.get('href');
            if (e.target.getDOM().tagName != 'A') {
                url = e.target.ancestor('a').get('href');
            }
            A.io.request('${bopsIdUrl}', {
                cache: false,
                sync: true,
                timeout: 5000,
                dataType: 'json',
                method: 'get',
                on: {
                    success: function() {
                        url += this.get("responseData");
                        window.open(url);
                    },
                    failure: function() {
                        window.open(url);
                    }
                }
            });
        });
    });
</script>