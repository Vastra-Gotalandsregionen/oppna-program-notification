<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="init.jsp" %>

<style type="text/css">
    .notification-list-item {
        padding: 4px;
    }

    .notification-list-item:hover {
        background-color: white;
    }
</style>

<h3>Alfresco</h3>
<c:forEach items="${sites}" var="site">
    <c:forEach items="${site.recentModifiedDocuments}" var="document">
        <div class="notification-list-item">
            <a href="${site.shareUrl}">${document.fileName}</a>
        </div>
    </c:forEach>
</c:forEach>
