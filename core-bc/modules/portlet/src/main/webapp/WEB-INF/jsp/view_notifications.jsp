<%@ include file="init.jsp" %>

<%--<c:choose>
	<c:when test="${notificationType == 'invoices'}">
		<h3>Mina fakturor</h3>
	</c:when>
	<c:when test="${notificationType == 'usd'}">
		<h3>Mina USD-&auml;renden</h3>
	</c:when>
	<c:when test="${notificationType == 'todo'}">
		<h3>Att g&ouml;ra</h3>
	</c:when>
	<c:when test="${notificationType == 'documents'}">
		<h3>Dokument</h3>
	</c:when>
	<c:when test="${notificationType == 'email'}">
		<h3>Epost</h3>
	</c:when>
	<c:otherwise>
		<h3>Notifikationer</h3>
	</c:otherwise>
</c:choose>--%>

<h3>${notificationType}</h3>

<p>Visa notifikationer nedan. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut sit amet neque metus, eget rutrum metus. Mauris a auctor ipsum. Nulla at iaculis ligula. In felis augue, venenatis sit amet molestie a, dictum vitae mauris.</p>