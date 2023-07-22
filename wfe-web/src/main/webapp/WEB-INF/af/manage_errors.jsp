<%@ page pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.Version"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script>
	var systemErrorsVisible = true;
	var processErrorsVisible = true;
	$(document).ready(function() {
		$("#systemErrorsButton").click(function() {
			if (systemErrorsVisible) {
				$("#systemErrorsContentDiv").hide();
				$("#systemErrorsImg").attr("src", "/wfe/images/view_setup_hidden.gif");
				systemErrorsVisible = false;
			} else {
				$("#systemErrorsContentDiv").show();
				$("#systemErrorsImg").attr("src", "/wfe/images/view_setup_visible.gif");
				systemErrorsVisible = true;
			}
		});
		$("#processErrorsButton").click(function() {
			if (processErrorsVisible) {
				$("#processErrorsContentDiv").hide();
				$("#processErrorsImg").attr("src", "/wfe/images/view_setup_hidden.gif");
				processErrorsVisible = false;
			} else {
				$("#processErrorsContentDiv").show();
				$("#processErrorsImg").attr("src", "/wfe/images/view_setup_visible.gif");
				processErrorsVisible = true;
			}
		});
		$("div.processErrorsFilter input").change(function() {
			var typeLabel = $(this).closest("label").text();
			var tds = $("#processErrorsContentDiv table tr").find("td:eq(0)").filter(":contains(" + typeLabel + ")");
			if ($(this).prop("checked")) {
				tds.closest("tr").show();
			} else {
				tds.closest("tr").hide();
			}
		});
	});
	</script>
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
			var input = $("table.view-setup tr[field='nodeType'] input[name='fieldsToFilterCriterias']");
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
			$("table.view-setup tr[field='nodeType'] img[class='button-more']").remove();
		});

		function activateProcesses(actionUrl) {
			var actionForm = $('form:eq(1)');
			actionForm.attr('action', actionUrl);
			actionForm.submit();
		}
	</script>

	<script type="text/javascript" src="<html:rewrite page='<%="/js/errorviewer.js?"+Version.getHash() %>' />">c=0;</script>
</tiles:put>

<tiles:put name="body" type="string">
<table class='box'><tr><th class='box'><bean:message key="title.errors" /></th></tr>
<tr><td class='box'>
	<div>
		<a id="systemErrorsButton" href="javascript:void(0)" class="link">
			<img id="systemErrorsImg" class="hideableblock" src="/wfe/images/view_setup_visible.gif">
			&nbsp;<bean:message key="errors.system" />
		</a>
	</div>
	<div id="systemErrorsContentDiv"> 
		<wf:viewSystemErrors />
	</div>
	<br />
	<div>
		<a id="processErrorsButton" href="javascript:void(0)" class="link">
			<img id="processErrorsImg" class="hideableblock" src="/wfe/images/view_setup_visible.gif">
			&nbsp;<bean:message key="errors.processes" />
		</a>
		<a href="/wfe/activateFailedProcesses.do" style="float: right;">
			<bean:message key="failed.processes.activate" />
		</a>
	</div>
	<div id="processErrorsContentDiv">
		<%
			String batchPresentationId = "listTokenErrorsForm";
			String returnAction = "/manage_errors.do";
		%>
		<wf:viewProcessErrors batchPresentationId="<%= batchPresentationId %>" buttonAlignment="right" returnAction="<%= returnAction %>">
			<div style="position: relative;">
				<wf:viewControlsHideableBlock hideableBlockId="<%= batchPresentationId %>" returnAction="<%= returnAction %>">
					<wf:tableViewSetupForm batchPresentationId="<%= batchPresentationId %>" returnAction="<%= returnAction %>" excelExportAction="/exportExcelTokenErrors"/>
				</wf:viewControlsHideableBlock>
			</div>
		</wf:viewProcessErrors>
	</div>
</td></tr>
<tr><td align='right' class='box'>
	<input value='<bean:message key="button.support" />' class='button' onclick="showSupportFiles();" type='button' <%= request.getAttribute("errorsExist") == null ? "disabled='disabled'" : "" %>>
</td></tr>
</table>
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>