<%@ page pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.Version"%>
<%@ page import="ru.runa.common.WebResources" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
	<%@include file="/WEB-INF/af/chat_notifications.jsp" %>

<tiles:put name="head" type="string">
<% if (WebResources.isBulkDeploymentElements()) { %>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.iframe-transport.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.fileupload.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/bulkuploadutils.js?"+Version.getHash() %>' />">c=0;</script>
	<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/fileupload.css?"+Version.getHash() %>' />">
<% } %>
</tiles:put>

<tiles:put name="body" type="string">

<%
	String returnAction = "/manage_reports.do";
%>

<wf:listReportsForm batchPresentationId="listReportsForm" buttonAlignment="right" returnAction="<%= returnAction %>" >
	<div style="position: relative;">
		<div>
			<wf:viewControlsHideableBlock hideableBlockId="listReportsForm" returnAction="<%= returnAction %>" >
				<wf:tableViewSetupForm batchPresentationId="listReportsForm" returnAction="<%= returnAction %>" />
			</wf:viewControlsHideableBlock>
		</div>
		<table width="100%">
			<tr>
				<td align="left">
					<wf:deployReportLink forward="deploy_report" />
				</td>
				<td align="right">
					<wf:managePermissionsLink securedObjectType="REPORTS" />
				</td>
			</tr>
		</table>
	</div>
</wf:listReportsForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>