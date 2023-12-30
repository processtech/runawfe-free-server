<%@ page pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.Version"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script type="text/javascript">
		var actionNode = "<bean:message key="node.type.ACTION_NODE" />";
		var endProcess = "<bean:message key="node.type.END_PROCESS" />";
		var waitState = "<bean:message key="node.type.WAIT_STATE" />";
		var timer = "<bean:message key="node.type.TIMER" />";
		var taskState = "<bean:message key="node.type.TASK_STATE" />";
		var fork = "<bean:message key="node.type.FORK" />";
		var join = "<bean:message key="node.type.JOIN" />";
		var decision = "<bean:message key="node.type.DECISION" />";
		var subprocess = "<bean:message key="node.type.SUBPROCESS" />";
		var multiSubprocess = "<bean:message key="node.type.MULTI_SUBPROCESS" />";
		var sendMessage = "<bean:message key="node.type.SEND_MESSAGE" />";
		var receiveMessage = "<bean:message key="node.type.RECEIVE_MESSAGE" />";
		var endToken = "<bean:message key="node.type.END_TOKEN" />";
		var multiTaskState = "<bean:message key="node.type.MULTI_TASK_STATE" />";
		var merge = "<bean:message key="node.type.MERGE" />";
		var exclusiveGateway = "<bean:message key="node.type.EXCLUSIVE_GATEWAY" />";
		var parallelGateway = "<bean:message key="node.type.PARALLEL_GATEWAY" />";
		$(document).ready(function() {
			var input = $("table#frozen-processes-conditions input[name='nodeType']");
			var select = $("<select />", { name: input.attr("name") });
			$("<option />", {val: "", text: ""}).appendTo(select);
			$("<option />", {val: "ACTION_NODE", text: actionNode}).appendTo(select);
			$("<option />", {val: "END_PROCESS", text: endProcess}).appendTo(select);
			$("<option />", {val: "WAIT_STATE", text: waitState}).appendTo(select);
			$("<option />", {val: "TIMER", text: timer}).appendTo(select);
			$("<option />", {val: "TASK_STATE", text: taskState}).appendTo(select);
			$("<option />", {val: "FORK", text: fork}).appendTo(select);
			$("<option />", {val: "JOIN", text: join}).appendTo(select);
			$("<option />", {val: "DECISION", text: decision}).appendTo(select);
			$("<option />", {val: "SUBPROCESS", text: subprocess}).appendTo(select);
			$("<option />", {val: "MULTI_SUBPROCESS", text: multiSubprocess}).appendTo(select);
			$("<option />", {val: "SEND_MESSAGE", text: sendMessage}).appendTo(select);
			$("<option />", {val: "RECEIVE_MESSAGE", text: receiveMessage}).appendTo(select);
			$("<option />", {val: "END_TOKEN", text: endToken}).appendTo(select);
			$("<option />", {val: "MULTI_TASK_STATE", text: multiTaskState}).appendTo(select);
			$("<option />", {val: "MERGE", text: merge}).appendTo(select);
			$("<option />", {val: "EXCLUSIVE_GATEWAY", text: exclusiveGateway}).appendTo(select);
			$("<option />", {val: "PARALLEL_GATEWAY", text: parallelGateway}).appendTo(select);
			select.val(input.val());
			input.replaceWith(select);
		});
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
<table class='box'><tr><th class='box'><bean:message key="title.frozen_processes" /></th></tr>
<tr>
	<td class='box'>
		<div id="frozenProcessesContentDiv">
			<%
				String returnAction = "/manage_frozen_processes.do";
			%>
			<wf:showFrozenProcesses buttonAlignment="left" returnAction="<%= returnAction %>" />
		</div>
	</td>
</tr>
</table>
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>
<script type="text/javascript">
	var messages = document.getElementById("errors-and-messages-container");
	var noFrozenMessage = document.getElementById("no-frozen-message");
	if (noFrozenMessage) {
		messages.appendChild(noFrozenMessage);
	}
</script>