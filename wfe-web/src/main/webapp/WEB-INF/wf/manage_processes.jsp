<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">

	<script type="text/javascript">
	var activeLabel = "<bean:message key="process.execution.status.active" />";
	var failedLabel = "<bean:message key="process.execution.status.failed" />";
	var suspendedLabel = "<bean:message key="process.execution.status.suspended" />";
	var endedLabel = "<bean:message key="process.execution.status.ended" />";
	$(document).ready(function() {
		var input = $("table.view-setup tr[field='batch_presentation.process.execution_status'] input[name='fieldsToFilterCriterias']");
		var select = $("<select />", { name: input.attr("name") });
		$("<option />", {val: "", text: ""}).appendTo(select);
		$("<option />", {val: "ACTIVE", text: activeLabel}).appendTo(select);
		$("<option />", {val: "FAILED", text: failedLabel}).appendTo(select);
		$("<option />", {val: "SUSPENDED", text: suspendedLabel}).appendTo(select);
		$("<option />", {val: "ENDED", text: endedLabel}).appendTo(select);
		select.val(input.val());
		input.replaceWith(select);
	});
	</script>
</tiles:put>

<tiles:put name="body" type="string">
<%
	String returnAction = "/manage_processes.do";
	String batchPresentationId = ru.runa.common.WebResources.isProcessTaskFiltersEnabled() ? "listProcessesWithTasksForm" : "listProcessesForm";
%>
<wf:listProcessesForm batchPresentationId="<%= batchPresentationId %>" returnAction="<%= returnAction %>">
    <div style="position: relative;">
        <div>
            <wf:viewControlsHideableBlock hideableBlockId="<%= batchPresentationId %>"  returnAction="<%= returnAction %>">
                <wf:tableViewSetupForm batchPresentationId="<%= batchPresentationId %>" returnAction="<%= returnAction %>" excelExportAction="/exportExcelProcesses" />
            </wf:viewControlsHideableBlock>
        </div>
        <div style="position: absolute; right: 5px; top: 5px;">
            <wf:managePermissionsLink securedObjectType="PROCESSES" />
        </div>
    </div>
</wf:listProcessesForm>
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>