<%@ include file="init.jsp" %>

<portlet:resourceURL var="resourceUrl" escapeXml="false">
    <portlet:param name="onlyCache" value="true"/>
</portlet:resourceURL>
<portlet:resourceURL var="resourceUrlNoCache" escapeXml="false">
    <portlet:param name="onlyCache" value="false"/>
</portlet:resourceURL>

<ul id="<portlet:namespace />notificationsBarList" class="notfications-bar-list clearfix">
    <c:if test="${!(invoicesCount > 0)}">
        <c:set var="hiddenClass" value="aui-helper-hidden"/>
    </c:if>
    <li class="notifications-bar-item notifications-bar-invoices first ${hiddenClass}" title="Fakturor">
        <portlet:renderURL var="invoicesURL">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="invoices"/>
        </portlet:renderURL>
        <a href="${invoicesURL}">
            <c:choose>
                <c:when test="${invoicesDisplayCount}">
                    <span id="invoices-count-wrapper" class="count"><span
                            id="invoices-count">${invoicesCount}</span></span>
                </c:when>
                <c:otherwise>
                    <span id="invoices-count-wrapper" class="count aui-helper-hidden"><span
                            id="invoices-count">${invoicesCount}</span></span>
                </c:otherwise>
            </c:choose>
            <span class="title">Mina fakturor</span>
        </a>
    </li>
    <c:if test="${!(usdIssuesCount > 0)}">
        <c:set var="hiddenClass" value="aui-helper-hidden"/>
    </c:if>
    <li class="notifications-bar-item notifications-bar-usd" title="USD">
        <portlet:renderURL var="usdURL">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="usdIssues"/>
        </portlet:renderURL>
        <a href="${usdURL}">
            <c:choose>
                <c:when test="${usdIssuesDisplayCount}">
                    <span id="usd-issues-count-wrapper" class="count"><span
                            id="usd-issues-count">${usdIssuesCount}</span></span>
                </c:when>
                <c:otherwise>
                    <span id="usd-issues-count-wrapper" class="count aui-helper-hidden"><span
                            id="usd-issues-count">${usdIssuesCount}</span></span>
                </c:otherwise>
            </c:choose>
            <span class="title">Mina USD-&auml;renden</span>
        </a>
    </li>
    <c:if test="${!(alfrescoCount > 0)}">
        <c:set var="hiddenClass" value="aui-helper-hidden"/>
    </c:if>
    <li class="notifications-bar-item notifications-bar-documents" title="Alfresco">
        <portlet:renderURL var="alfrescoUrl">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="alfresco"/>
        </portlet:renderURL>
        <a href="${alfrescoUrl}">
            <c:choose>
                <c:when test="${alfrescoDisplayCount}">
                    <span id="alfresco-count-wrapper" class="count"><span
                            id="alfresco-count">${alfrescoCount}</span></span>
                </c:when>
                <c:otherwise>
                    <span id="alfresco-count-wrapper" class="count aui-helper-hidden"><span
                            id="alfresco-count">${alfrescoCount}</span></span>
                </c:otherwise>
            </c:choose>
            <span class="title">Dokument</span>
        </a>
    </li>
    <c:if test="${!(randomCount > 0)}">
        <c:set var="hiddenClass" value="aui-helper-hidden"/>
    </c:if>
    <li class="notifications-bar-item notifications-bar-documents" title="Random">
        <portlet:renderURL var="randomUrl">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="random"/>
        </portlet:renderURL>
        <a href="${randomUrl}">
            <c:choose>
                <c:when test="${randomDisplayCount}">
                    <span id="random-count-wrapper" class="count"><span id="random-count">${randomCount}</span></span>
                </c:when>
                <c:otherwise>
                    <span id="random-count-wrapper" class="count aui-helper-hidden"><span
                            id="random-count">${randomCount}</span></span>
                </c:otherwise>
            </c:choose>
            <span class="title">Random</span>
        </a>
    </li>
    <c:if test="${!(emailCount > 0)}">
        <c:set var="hiddenClass" value="aui-helper-hidden"/>
    </c:if>
    <li class="notifications-bar-item notifications-bar-email last" title="E-post">
        <portlet:renderURL var="emailURL">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="email"/>
        </portlet:renderURL>
        <a href="${emailURL}">
            <c:choose>
                <c:when test="${emailDisplayCount}">
                    <span id="email-count-wrapper" class="count"><span id="email-count">${emailCount}</span></span>
                </c:when>
                <c:otherwise>
                    <span id="email-count-wrapper" class="count aui-helper-hidden"><span
                            id="email-count">${emailCount}</span></span>
                </c:otherwise>
            </c:choose>
            <span class="title">Epost</span>
        </a>
    </li>
</ul>

<liferay-util:html-bottom>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/notifications-bar.js"></script>
    <script type="text/javascript">

        var a; //TODO finns det bättre sätt?

        AUI().ready('aui-base', 'rp-notifications-bar', function (A) {

            var notificationsBar = new A.NotificationsBar({
                notificationsListNode:'#<portlet:namespace />notificationsBarList'
            });

            notificationsBar.render();

            a = A;
            window.setInterval("reloadNotifications('${resourceUrl}')", ${interval});
            reloadNotifications('${resourceUrlNoCache}');
        });

    </script>
</liferay-util:html-bottom>