<%@page session="false" contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0" %>

<style type="text/css">
    .alfresco-documents {
        background-color: white;
        position: absolute;
        width: 300px;
        height: 200px;
        border: 2px solid;
        border-radius: 3px;
    }
</style>

<portlet:resourceURL var="resourceUrl" escapeXml="false"/>
<portlet:resourceURL id="alfrescoResource" var="alfrescoResourceUrl" escapeXml="false"/>

<div>Alfresco = <span id="alfresco-count">${alfrescoCount}</span>
    <button id="toggle-alfresco-documents-button" onclick="toggleAlfrescoDocuments()">Visa</button>
</div>
<div id="alfresco-documents" class="alfresco-documents" style="visibility: hidden;">
</div>
<div>Usd = <span id="usd-issues-count">${usdIssuesCount}</span></div>
<div>Random = <span id="random-value">${randomCount}</span></div>
<div>E-post = <span id="email-count">${emailCount}</span></div>
<div>Fakturor = <span id="invoices-count">${invoicesCount}</span></div>

<script type="text/javascript">

    var a;
    AUI().ready('aui-base',
            function (A) {
                a = A;
                window.setInterval("reloadNotifications('${resourceUrl}')", ${interval});
            });

    function reloadNotifications(resourceUrl) {
        var items = null;
        a.io.request(resourceUrl, {
            cache:false,
            sync:true,
            timeout:1000,
            dataType:'json',
            method:'get',
            on:{
                success:function () {
                    items = this.get('responseData');

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

                    var element = document.getElementById('random-value');
                    var value = items['randomCount'];
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
        var bool = value != element.innerHTML;
        if (value != element.innerHTML) {
            // New value -> highlight
            element.style.fontWeight = "bold";
            element.style.fontSize = "1.4em";
        } else {
            element.style.fontWeight = "normal";
            element.style.fontSize = "1.0em";
        }
    }

    function toggleAlfrescoDocuments(url) {
        var button = document.getElementById("toggle-alfresco-documents-button");

        if (button.innerHTML == "Visa") {
            a.io.request("${alfrescoResourceUrl}", {
                cache:false,
                sync:true,
                timeout:1000,
                dataType:'json',
                method:'get',
                on:{
                    success:function () {
                        items = this.get('responseData');

                        var html = "<div id='alfresco-documents-content'>";
                        for (i = 0; i < items.length; i++) {
                            html += "<table>"
                            html += "<tr>"
                            html += "<td>Titel:</td><td>" + items[i]['fileName'] + "</td>";
                            html += "</tr>";
                            html += "<tr>"
                            html += "<td>Skapad av:</td><td>" + items[i]['createdByUser'] + "</td>";
                            html += "</tr>";
                            html += "</table>";
                            html += "<br/>";
                        }
                        html += "</div>";
                        console.log("html: " + html);
                        console.log(items);

                        var element = document.getElementById("alfresco-documents");
                        element.innerHTML = html;
                        element.style.visibility = "visible";
                        button.innerHTML = "Göm";
                    },
                    failure:function () {
                    }
                }
            })
        } else {
            var element = document.getElementById("alfresco-documents");
            element.style.visibility = "hidden";
            button.innerHTML = "Visa";
        }
    }


</script>