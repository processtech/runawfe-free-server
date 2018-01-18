<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ page import="ru.runa.common.WebResources" %>
<%@ page import="ru.runa.wf.web.tag.ListTasksFormTag" %>
<%@ page import="ru.runa.wf.web.form.ProcessForm" %>
<%@ page import="ru.runa.common.web.Commons" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script type="text/javascript" src="/wfe/js/i18n/delegate.dialog-<%= Commons.getLocale(pageContext).getLanguage() %>.js">c=0;</script>
    <script type="text/javascript" src="<html:rewrite page="/js/delegate.dialog.js" />">c=0;</script>
    <link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/delegate.dialog.css" />">
	<meta http-equiv="refresh" content="180; URL='<html:rewrite action="/administer_tasks.do?tabForwardName=administer_tasks"/>'">
</tiles:put>
<tiles:put name="body" type="string">
<%
	String returnAction = "/administer_tasks.do";
%>
<wf:listTasksAdministerForm batchPresentationId="listTasksAdministerForm" buttonAlignment="right" returnAction="<%= returnAction %>" >
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
		<wf:viewControlsHideableBlock hideableBlockId="listTasksAdministerForm"  returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listTasksAdministerForm" returnAction="<%= returnAction %>" excelExportAction="/exportExcelTasks" />
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
</wf:listTasksAdministerForm>

	<% if (WebResources.isTaskDelegationEnabled()) { %>
	<%-- taskId = -1L - means that multiple delegation are processed --%>
	<wf:taskFormDelegationButton taskId="<%= -1L %>" />
	<% } %>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>