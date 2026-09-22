<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script type="text/javascript">
        function startSearch(actionUrl) {
            showLoading();
            var actionForm = $('form:eq(0)');
            actionForm.attr('action', actionUrl);
            actionForm.submit();
        }
        function showLoading() {
            $("#errors-and-messages-container")
                .html(
                    "<img src=\"/wfe/images/loading.gif\" align=\"absmiddle\">&nbsp;&nbsp;&nbsp;<span style='font-weight: bold; color: blue;'>"
                        + loadingMessage + "</span>"
                );
        }
	</script>
</tiles:put>

<tiles:put name="body" type="string">
    <wf:processSearchForm />
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />

</tiles:insert>
<script type="text/javascript">
	var messages = document.getElementById("errors-and-messages-container");
	var noProcessMessage = document.getElementById("no-process-message");
	if (noProcessMessage) {
		messages.appendChild(noProcessMessage);
	}
</script>