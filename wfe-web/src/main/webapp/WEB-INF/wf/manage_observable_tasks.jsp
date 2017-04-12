<%@page import="ru.runa.common.web.form.IdForm"%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ page import="ru.runa.common.WebResources" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	String id = request.getParameter(parameterName);
	id = (id == null ? "0" : id);
	String returnAction = "/manage_observable_tasks.do";
%>
<wf:listObservableTasksForm batchPresentationId="listObservableTasksForm" buttonAlignment="right" returnAction="<%= returnAction %>" executorId="<%= Long.parseLong(id) %>" >
	<script>
	var helpVisible = false;
	$().ready(function() {
		$("#helpRequestButton").click(function() {
			if (helpVisible) {
				$("#helpContentDiv").hide();
				$("#helpImg").attr("src", "/wfe/images/view_setup_hidden.gif");
				helpVisible = false;
			} else {
				$("#helpContentDiv").show();
				$("#helpImg").attr("src", "/wfe/images/view_setup_visible.gif");
				helpVisible = true;
			}
		});
	});
	</script>
	<div style="position: relative;">
		<div style="position: absolute; right: 5px; top: 5px;">
			<table><tbody><tr>
				<td class="hideableblock">
					<a href="#" class="hideableblock" id="helpRequestButton">
						<img id="helpImg" class="hideableblock" src="/wfe/images/view_setup_hidden.gif">&nbsp;<bean:message key="link.help" />
					</a>
				</td>
			</tr></tbody></table>
		</div>
		<wf:viewControlsHideableBlock hideableBlockId="listObservableTasksForm"  returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listObservableTasksForm" returnAction="<%= returnAction %>" excelExportAction="/exportExcelTasks" />
		</wf:viewControlsHideableBlock>
		<div id="helpContentDiv" style="display: none;">
			<table>
				<tr class="deadlineAlmostExpired">
					<td style="width: 100px; border: 1px solid gray; margin: 5px;">&nbsp;</td>
					<td class="help" style="background-color: white;"><bean:message key="tasks.help.deadlineAlmostExpired" /> (<%= WebResources.getTaskExpiredWarningThreshold() %>)</td>
				</tr>
				<tr class="deadlineExpired">
					<td style="width: 100px; border: 1px solid gray; margin: 5px;">&nbsp;</td>
					<td class="help" style="background-color: white;"><bean:message key="tasks.help.deadlineExpired" /></td>
				</tr>
				<tr class="escalatedTask">
					<td style="width: 100px; border: 1px solid gray; margin: 5px;">&nbsp;</td>
					<td class="help" style="background-color: white;"><bean:message key="tasks.help.escalatedTask" /></td>
				</tr>
				<tr class="substitutionTask">
					<td style="width: 100px; border: 1px solid gray; margin: 5px;">&nbsp;</td>
					<td class="help" style="background-color: white;"><bean:message key="tasks.help.substitutionTask" /></td>
				</tr>
			</table>
		</div>
	</div>	
</wf:listObservableTasksForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
<tiles:put name="head" type="string">
	<meta http-equiv="refresh" content="180; URL='<html:rewrite action="/manage_observable_tasks.do?tabForwardName=manage_observable_tasks"/>'">
</tiles:put>
</tiles:insert>