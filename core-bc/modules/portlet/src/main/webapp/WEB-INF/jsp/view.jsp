<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="init.jsp" %>

<portlet:resourceURL var="resourceUrl" escapeXml="false">
    <portlet:param name="onlyCache" value="true"/>
</portlet:resourceURL>
<portlet:resourceURL var="resourceUrlNoCache" escapeXml="false">
    <portlet:param name="onlyCache" value="false"/>
</portlet:resourceURL>

<c:set var="cssClassHidden" value="aui-helper-hidden" scope="page"/>
<c:set var="cssClassCountHighlight" value="count-highlight" scope="page"/>

<ul id="<portlet:namespace />notificationsBarList" class="notfications-bar-list clearfix">

        <c:if test="${!(invoicesCount.count > 0)}">
            <c:set var="cssClassHiddenInvoices" value="${cssClassHidden}"/>
        </c:if>
        <li id="<portlet:namespace />itemInvoices"
            class="notifications-bar-item notifications-bar-invoices first ${cssClassHiddenInvoices}" title="Fakturor">
            <portlet:renderURL var="invoicesURL" windowState="exclusive">
                <portlet:param name="action" value="showExpandedNotifications"/>
                <portlet:param name="notificationType" value="invoices"/>
            </portlet:renderURL>
            <a href="${invoicesURL}">
                <c:if test="${invoicesHighlightCount}">
                    <c:set var="cssClassCountWrapperInvoices" value="${cssClassCountHighlight}"/>
                </c:if>
            <span class="count ${cssClassCountWrapperInvoices}">
				<span>${invoicesCount.count}</span>
            </span>
                <span class="title">Mina fakturor</span>
            </a>
        </li>

        <c:if test="${!(usdIssuesCount.count > 0)}">
            <c:set var="cssClassHiddenUsd" value="${cssClassHidden}"/>
        </c:if>
        <li id="<portlet:namespace />itemUsd" class="notifications-bar-item notifications-bar-usd ${cssClassHiddenUsd}"
            title="Ärenden VGR IT">
            <portlet:renderURL var="usdURL" windowState="exclusive">
                <portlet:param name="action" value="showExpandedNotifications"/>
                <portlet:param name="notificationType" value="usdIssues"/>
            </portlet:renderURL>
            <a href="${usdURL}">
                <c:if test="${usdIssuesHighlightCount}">
                    <c:set var="cssClassCountWrapperUsd" value="${cssClassCountHighlight}"/>
                </c:if>
            <span class="count ${cssClassCountWrapperUsd}">
				<span>${usdIssuesCount.count}</span>
            </span>
                <span class="title">&Auml;renden i Navet</span>
            </a>
        </li>

        <c:if test="${!(alfrescoCount.count > 0)}">
            <c:set var="cssClassHiddenAlfresco" value="${cssClassHidden}"/>
        </c:if>
        <li id="<portlet:namespace />itemAlfresco"
            class="notifications-bar-item notifications-bar-documents ${cssClassHiddenAlfresco}" title="Alfresco">
            <portlet:renderURL var="alfrescoUrl" windowState="exclusive">
                <portlet:param name="action" value="showExpandedNotifications"/>
                <portlet:param name="notificationType" value="alfresco"/>
            </portlet:renderURL>
            <a href="${alfrescoUrl}">
                <c:if test="${alfrescoHighlightCount}">
                    <c:set var="cssClassCountWrapperAlfresco" value="${cssClassCountHighlight}"/>
                </c:if>
            <span class="count ${cssClassCountWrapperAlfresco}">
				<span>${alfrescoCount.count}</span>
            </span>
                <span class="title">Dokument</span>
            </a>
        </li>

        <c:if test="${!(emailCount.count > 0)}">
            <c:set var="cssClassHiddenEmail" value="${cssClassHidden}"/>
        </c:if>
        <li id="<portlet:namespace />itemEmail"
            class="notifications-bar-item notifications-bar-email ${cssClassHiddenEmail}" title="E-post  dafs ${ewsEmailCount.count}">
            <portlet:renderURL var="emailURL" windowState="exclusive">
                <portlet:param name="action" value="showExpandedNotifications"/>
                <portlet:param name="notificationType" value="email"/>
            </portlet:renderURL>
            <a href="${emailURL}">
                <c:if test="${emailHighlightCount}">
                    <c:set var="cssClassCountWrapperEmail" value="${cssClassCountHighlight}"/>
                </c:if>
            <span class="count ${cssClassCountWrapperEmail}">
                <c:choose>
                    <c:when test="${not empty emailCount.count}">
                        <span>${emailCount.count}</span>
                    </c:when>
                    <c:when test="${not empty emailCount.message}">
                        <span>!</span>
                    </c:when>
                    <c:otherwise>
                        <span></span>
                    </c:otherwise>
                </c:choose>
            </span>
                <span class="title">E-post daf ${ewsEmailCount.count}</span>
            </a>
        </li>

        <c:if test="${!(ewsEmailCount.count > 0)}">
            <c:set var="cssClassHiddenEmail" value="${cssClassHidden}"/>
        </c:if>
        <li id="<portlet:namespace />itemEwsEmail"
            class="notifications-bar-item notifications-bar-email ${cssClassHiddenEmail}" title="E-post">
            <portlet:renderURL var="ewsEmailURL" windowState="exclusive">
                <portlet:param name="action" value="showExpandedNotifications"/>
                <portlet:param name="notificationType" value="ewsEmail"/>
            </portlet:renderURL>
            <a href="${ewsEmailURL}">
                <c:if test="${ewsEmailHighlightCount}">
                    <c:set var="cssClassCountWrapperEwsEmail" value="${cssClassCountHighlight}"/>
                </c:if>
            <span class="count ${cssClassCountWrapperEwsEmail}">
                <c:choose>
                    <c:when test="${not empty ewsEmailCount.count}">
                        <span>${ewsEmailCount.count}</span>
                    </c:when>
                    <c:when test="${not empty ewsEmailCount.message}">
                        <span>!</span>
                    </c:when>
                    <c:otherwise>
                        <span></span>
                    </c:otherwise>
                </c:choose>
            </span>
                <span class="title">E-post</span>
            </a>
        </li>

        <c:if test="${!(medControlCount.count > 0)}">
            <c:set var="cssClassHiddenMedControl" value="${cssClassHidden}"/>
        </c:if>
        <li id="<portlet:namespace />itemMedControl"
            class="notifications-bar-item notifications-bar-med-control ${cssClassHiddenMedControl}" title="MedControl">
            <portlet:renderURL var="medControlURL" windowState="exclusive">
                <portlet:param name="action" value="showExpandedNotifications"/>
                <portlet:param name="notificationType" value="medControl"/>
            </portlet:renderURL>
            <a href="${medControlURL}">
                <c:if test="${medControlHighlightCount}">
                    <c:set var="cssClassCountWrapperMedControl" value="${cssClassCountHighlight}"/>
                </c:if>
            <span class="count ${cssClassCountWrapperMedControl}">
				<span>${medControlCount.count}</span>
            </span>
                <span class="title">MedControl</span>
            </a>
        </li>

        <c:if test="${!(socialRequestCount.count > 0)}">
            <c:set var="cssClassHiddenRequests" value="${cssClassHidden}"/>
        </c:if>
        <li id="<portlet:namespace />itemSocialRequests"
            class="notifications-bar-item notifications-bar-social-requests last ${cssClassHiddenRequests}"
            title="Vänförfrågningar">
            <portlet:renderURL var="socialRequestsURL" windowState="exclusive">
                <portlet:param name="action" value="showExpandedNotifications"/>
                <portlet:param name="notificationType" value="socialRequests"/>
            </portlet:renderURL>
            <a href="${socialRequestsURL}">
                <c:if test="${socialRequestHighlightCount}">
                    <c:set var="cssClassCountWrapperSocialRequests" value="${cssClassCountHighlight}"/>
                </c:if>
            <span class="count ${cssClassCountWrapperSocialRequests}">
				<span>${socialRequestCount.count}</span>
            </span>
                <span class="title">Vänförfrågningar</span>
            </a>
        </li>

    </ul>

<liferay-util:html-bottom>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/notifications-bar.js"></script>
    <script type="text/javascript">

        AUI().ready('aui-base', 'rp-notifications-bar', function (A) {

            var notificationsBar = new A.NotificationsBar({

                nodeItemInvoices:'#<portlet:namespace />itemInvoices',
                nodeItemUsd:'#<portlet:namespace />itemUsd',
                nodeItemAlfresco:'#<portlet:namespace />itemAlfresco',
                nodeItemEmail:'#<portlet:namespace />itemEmail',
                nodeItemEwsEmail:'#<portlet:namespace />itemEwsEmail',
                nodeItemMedControl:'#<portlet:namespace />itemMedControl',
                nodeItemSocialRequest:'#<portlet:namespace />itemSocialRequests',

                notificationsListNode:'#<portlet:namespace />notificationsBarList',
                updateNotificationsInterval: ${interval},
                updateNotificationsUrl:'${resourceUrl}',
                updateNotificationsNoCacheUrl:'${resourceUrlNoCache}'
            });

            notificationsBar.render();
        });

    </script>
</liferay-util:html-bottom>