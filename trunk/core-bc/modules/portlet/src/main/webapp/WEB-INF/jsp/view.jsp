<%@ include file="init.jsp" %>

<portlet:resourceURL var="resourceUrl" escapeXml="false">
    <portlet:param name="onlyCache" value="true"/>
</portlet:resourceURL>
<portlet:resourceURL var="resourceUrlNoCache" escapeXml="false">
    <portlet:param name="onlyCache" value="false"/>
</portlet:resourceURL>

<c:set var="cssClassHidden" value="aui-helper-hidden" scope="page" />

<ul id="<portlet:namespace />notificationsBarList" class="notfications-bar-list clearfix">

    <c:if test="${!(invoicesCount > 0)}">
        <c:set var="cssClassHiddenInvoices" value="${cssClassHidden}"/>
    </c:if>
    <li id="<portlet:namespace />itemInvoices" class="notifications-bar-item notifications-bar-invoices first ${cssClassHiddenInvoices}" title="Fakturor">
        <portlet:renderURL var="invoicesURL">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="invoices"/>
        </portlet:renderURL>
        <a href="${invoicesURL}">
		    <c:if test="${!(invoicesDisplayCount)}">
		        <c:set var="cssClassCountWrapperInvoices" value="${cssClassHidden}"/>
		    </c:if>
            <span class="count ${cssClassCountWrapperInvoices}">
				<span>${invoicesCount}</span>
            </span>
            <span class="title">Mina fakturor</span>
        </a>
    </li>
    
    <c:if test="${!(invoicesCount > 0)}">
        <c:set var="cssClassHiddenUsd" value="${cssClassHidden}"/>
    </c:if>
    <li id="<portlet:namespace />itemUsd" class="notifications-bar-item notifications-bar-usd ${cssClassHiddenUsd}" title="USD">
        <portlet:renderURL var="usdURL">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="usdIssues"/>
        </portlet:renderURL>
        <a href="${usdURL}">
		    <c:if test="${!(usdIssuesDisplayCount)}">
		        <c:set var="cssClassCountWrapperUsd" value="${cssClassHidden}"/>
		    </c:if>
            <span class="count ${cssClassCountWrapperUsd}">
				<span>${usdIssuesDisplayCount}</span>
            </span>
            <span class="title">Mina USD-&auml;renden</span>
        </a>
    </li>
    
    <c:if test="${!(alfrescoCount > 0)}">
        <c:set var="cssClassHiddenAlfresco" value="${cssClassHidden}"/>
    </c:if>
    <li id="<portlet:namespace />itemAlfresco" class="notifications-bar-item notifications-bar-documents ${cssClassHiddenAlfresco}" title="Alfresco">
        <portlet:renderURL var="alfrescoUrl">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="alfresco"/>
        </portlet:renderURL>
        <a href="${alfrescoUrl}">
		    <c:if test="${!(alfrescoDisplayCount)}">
		        <c:set var="cssClassCountWrapperAlfresco" value="${cssClassHidden}"/>
		    </c:if>
            <span class="count ${cssClassCountWrapperAlfresco}">
				<span>${alfrescoCount}</span>
            </span>
            <span class="title">Dokument</span>
        </a>
    </li>

    <c:if test="${!(randomCount > 0)}">
        <c:set var="cssClassHiddenRandom" value="${cssClassHidden}"/>
    </c:if>
    <li id="<portlet:namespace />itemRandom" class="notifications-bar-item notifications-bar-documents ${cssClassHiddenRandom}" title="Random">
        <portlet:renderURL var="randomUrl">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="random"/>
        </portlet:renderURL>
        <a href="${randomUrl}">
		    <c:if test="${!(randomDisplayCount)}">
		        <c:set var="cssClassCountWrapperRandom" value="${cssClassHidden}"/>
		    </c:if>
            <span class="count ${cssClassCountWrapperRandom}">
				<span>${randomCount}</span>
            </span>
            <span class="title">Random</span>
        </a>
    </li>

    <c:if test="${!(emailCount > 0)}">
        <c:set var="cssClassHiddenEmail" value="${cssClassHiddenEmail}"/>
    </c:if>
    <li id="<portlet:namespace />nodeItemEmail" class="notifications-bar-item notifications-bar-email last ${cssClassHiddenEmail}" title="E-post">
        <portlet:renderURL var="emailURL">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="email"/>
        </portlet:renderURL>
        <a href="${emailURL}">
		    <c:if test="${!(emailDisplayCount)}">
		        <c:set var="cssClassCountWrapperEmail" value="${cssClassHidden}"/>
		    </c:if>
            <span class="count ${cssClassCountWrapperEmail}">
				<span>${emailCount}</span>
            </span>
            <span class="title">E-post</span>
        </a>
    </li>
    
</ul>

<liferay-util:html-bottom>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/notifications-bar.js"></script>
    <script type="text/javascript">

        AUI().ready('aui-base', 'rp-notifications-bar', function (A) {

            var notificationsBar = new A.NotificationsBar({
            	
            	nodeItemInvoices: '#<portlet:namespace />itemInvoices',
            	nodeItemUsd: '#<portlet:namespace />itemUsd',
            	nodeItemAlfresco: '#<portlet:namespace />itemAlfresco',
            	nodeItemRandom: '#<portlet:namespace />itemRandom',
            	nodeItemEmail: '#<portlet:namespace />itemEmail',            	
            	
                notificationsListNode:'#<portlet:namespace />notificationsBarList',
                updateNotificationsInterval: ${interval},
                updateNotificationsUrl: '${resourceUrl}',
                updateNotificationsNoCacheUrl: '${resourceUrlNoCache}'
            });

            notificationsBar.render();
        });

    </script>
</liferay-util:html-bottom>