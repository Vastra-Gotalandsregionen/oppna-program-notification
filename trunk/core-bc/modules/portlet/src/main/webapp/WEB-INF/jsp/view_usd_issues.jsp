<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="init.jsp" %>

<portlet:resourceURL var="bopsIdUrl" id="lookupBopsId" />

<style type="text/css">
    .notification-list-item {
        padding: 4px;
    }

    .notification-list-item:hover {
        background-color: white;
    }
    
    .notification-list-item a {
        text-decoration: none;
    }
</style>

<h3>USD-ärenden</h3>
<%--
<c:forEach items="${usdIssues}" var="usdIssue">
    ${usdIssue.summary}</p>
</c:forEach>
--%>

<%--<ul class="usd-issues-list">--%>
<div class="usd-issues">
    <c:forEach items="${usdIssues}" var="usdIssue">
        <%--<li>--%>
            <%--<span>--%>
        <div class="notification-list-item">
                <a href="${usdIssue.url}" target="_blank">${usdIssue.type} - ${usdIssue.summary}</a>
        </div>
            <%--</span>--%>
        <%--</li>--%>
    </c:forEach>
</div>
<%--</ul>--%>

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