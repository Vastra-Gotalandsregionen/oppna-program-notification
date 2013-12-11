<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tld/util.tld" prefix="util" %>

<%@ include file="init.jsp" %>

<style type="text/css">
    .notification-email-summary {
        display: block;
        margin: 0 0 0 10px;
        float: left;
        width: 280px;
    }

    .notification-email-short-info {
        display: block;
        float: right;
    }
</style>
<div class="notification-wrap">
	<h3>E-post</h3>
	${message}

    <ul class="notifications-list">
        <c:forEach items="${emails}" var="email" varStatus="iteratorStatus">
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
                <a href="${website}" target="_blank" class="clearfix">
                    <span class="notification-email-summary"><div><b>${email.from.mailbox.name}</b></div> - ${email.subject}</span>
                    <span class="notification-email-short-info">
                        <b>${util:xmlGregorianCalendarToNiceString(email.dateTimeSent)}</b>
                    </span>
                </a>
            </li>
        </c:forEach>
    </ul>
</div>