<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0" %>


<div>Alfresco count = ${numberNewAlfresco}</div>
<div>Usd count = ${numberUsdIssues}</div>
<div>Random = <span id="random-value">${slowRandom}</span></div>

<portlet:resourceURL var="resourceUrl" escapeXml="false"/>

<script type="text/javascript">

    var a;
    AUI().ready('aui-base',
            function (A) {
                a = A;
                window.setInterval("reloadNotifications('${resourceUrl}')", ${interval});


            })
    function reloadNotifications() {
        alert("rel");
        reloadNotifications('${resourceUrl}');
    }

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
                    console.log(items);
                    console.log(items['slowRandom']);
                    var randomDiv = document.getElementById('random-value');
                    randomDiv.innerHTML = items['slowRandom'];


                },
                failure:function () {
                    alert('Failure');
                }
            }
        });
    }

</script>