<%@ include file="init.jsp" %>

<portlet:resourceURL var="resourceUrl" escapeXml="false"/>

<ul id="<portlet:namespace />notificationsBarList" class="notfications-bar-list clearfix">
    <%--<c:if test="${not empty invoicesCount}">--%>
    <li class="notifications-bar-item notifications-bar-invoices first" title="Fakturor">
        <portlet:renderURL var="invoicesURL">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="invoices"/>
        </portlet:renderURL>
        <a href="${invoicesURL}">
            <%--<c:if test="${invoicesDisplayCount}">
            <span id="invoices-count-wrapper" class="count"><span id="invoices-count">${invoicesCount}</span></span>
            </c:if>--%>
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
    <%--</c:if>--%>
    <%--<c:if test="${not empty usdIssuesCount}">--%>
    <li class="notifications-bar-item notifications-bar-usd" title="USD">
        <portlet:renderURL var="usdURL">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="usdIssues"/>
        </portlet:renderURL>
        <a href="${usdURL}">
            <%--<c:if test="${usdIssuesDisplayCount}">
            <span id="usd-issues-count-wrapper" class="count"><span id="usd-issues-count">${usdIssuesCount}</span></span>
            </c:if>--%>
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
    <%--</c:if>--%>
    <%--<c:if test="${not empty todoCount}">--%>
    <li class="notifications-bar-item notifications-bar-todo" title="Att g&ouml;ra">
        <portlet:renderURL var="todoURL">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="todo"/>
        </portlet:renderURL>
        <a href="${todoURL}">
            <%--<c:if test="${randomDisplayCount}">
                <span id="todo-count-wrapper" class="count"><span id="todo-count">${todoCount}</span></span>
            </c:if>--%>
            <c:choose>
                <c:when test="${todoDisplayCount}">
                    <span id="todo-count-wrapper" class="count"><span id="todo-count">${todoCount}</span></span>
                </c:when>
                <c:otherwise>
                    <span id="todo-count-wrapper" class="count aui-helper-hidden"><span
                            id="todo-count">${todoCount}</span></span>
                </c:otherwise>
            </c:choose>
            <span class="title">Att g&ouml;ra</span>
        </a>
    </li>
    <%--</c:if>--%>
    <%--<c:if test="${not empty alfrescoCount}">--%>
    <li class="notifications-bar-item notifications-bar-documents" title="Alfresco">
        <portlet:renderURL var="alfrescoUrl">
            <portlet:param name="action" value="showExpandedNotifications"/>
            <portlet:param name="notificationType" value="alfresco"/>
        </portlet:renderURL>
        <a href="${alfrescoUrl}">
            <%--<c:if test="${alfrescoDisplayCount}">
                <span id="alfresco-count-wrapper" class="count"><span id="alfresco-count">${alfrescoCount}</span></span>
            </c:if>--%>
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
    <%--</c:if>--%>
    <%--<c:if test="${not empty randomCount}">--%>
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
            <%--
                            <c:if test="${randomDisplayCount}">
                            <span id="random-count-wrapper" class="count"><span id="random-count">${randomCount}</span></span>
                            </c:if>
            --%>
            <span class="title">Random</span>
        </a>
    </li>
    <%--</c:if>--%>
    <%--<c:if test="${not empty emailCount}">--%>
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
            <%-- <c:if test="${emailDisplayCount}">

            </c:if>--%>
            <span class="title">Epost</span>
        </a>
    </li>
    <%--</c:if>--%>
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

        });

        /*function reloadNotifications(resourceUrl) {
         var items = null;
         a.io.request(resourceUrl, {
         cache:false,
         sync:true,
         timeout:1000,
         dataType:'json',
         method:'get',
         on:{
         success:function () {
         console.log("success");
         items = this.get('responseData');
         console.log("success2");

         var element = document.getElementById('alfresco-count');
         var value = items['alfrescoCount'];
         checkNewValue(value, element);
         element.innerHTML = value;

         var element = document.getElementById('usd-issues-count');
         var value = items['usdIssuesCount'];
         checkNewValue(value, element);
         element.innerHTML = value;

         var element = document.getElementById('email-count');
         var value = items['emailCount'];
         checkNewValue(value, element);
         element.innerHTML = value;

         var element = document.getElementById('random-count');
         var value = items['randomCount'];
         console.log(value + " " + element.innerHTML);
         checkNewValue(value, element);
         element.innerHTML = value;

         var element = document.getElementById('invoices-count');
         var value = items['invoicesCount'];
         checkNewValue(value, element);
         element.innerHTML = value;

         },
         failure:function () {
         }
         }
         });
         }

         function checkNewValue(value, element) {
         console.log(element + " " + element.parentNode);
         var bool = value != element.innerHTML;
         if (value != element.innerHTML) {
         // New value -> highlight
         //                element.style.fontWeight = "bold";
         element.style.fontSize = "1.4em";
         console.log(element + " " + element.parentNode);
         element.parentNode.className = "count";//('aui-helper-hidden');
         } else {
         //                element.style.fontWeight = "bold";
         element.style.fontSize = "0.9em";
         //                element.ancestor.className = "count aui-helper-hidden";

         }
         }*/

    </script>
</liferay-util:html-bottom>