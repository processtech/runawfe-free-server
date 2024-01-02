<%@page import="ru.runa.common.Version"%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
<script type="text/javascript" src="<html:rewrite page='<%="/js/processgraphutils.js?"+Version.getHash() %>' />">c=0;</script>
</tiles:put>

<tiles:put name="body" type="string">
<%
	String returnAction = "/manage_process_execution.do";
%>
<wf:processInfoForm identifiableId="${param.processId}" readOnly="true">
	<table width="100%">
		<tr>
			<td align="right">
				<wf:showProcessLink identifiableId="${param.processId}" />
			</td>
		</tr>
	</table>
</wf:processInfoForm>
<wf:moveTokenForm processId="${param.processId}" />
<wf:createTokenForm processId="${param.processId}" />
<wf:listTokensForm batchPresentationId="listTokensForm" processId="${param.processId}" returnAction="<%= returnAction %>" action="<%= returnAction %>" />
<script>
currentNodeIdInput = "";
function selectProcessNode(nodeId, close) {
	if(close) {
		for(var i = 1; i <= graphDialogCounter; i++) {
			$("#graphDialog" + i).dialog("close");
		}
	}
	$("#" + currentNodeIdInput)[0].value = nodeId;
}
</script>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>