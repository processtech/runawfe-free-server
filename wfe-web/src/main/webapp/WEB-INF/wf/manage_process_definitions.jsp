<%@ page pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.Version"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<%@ page import="ru.runa.common.WebResources" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

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
	String returnAction = "/manage_process_definitions.do";
%>

<wf:listProcessesDefinitionsForm  batchPresentationId="listProcessesDefinitionsForm" buttonAlignment="right" returnAction="<%= returnAction %>" >
	<div style="position: relative;">
		<div>
			<wf:viewControlsHideableBlock hideableBlockId="listProcessesDefinitionsForm" returnAction="<%= returnAction %>" >
				<wf:tableViewSetupForm batchPresentationId="listProcessesDefinitionsForm" returnAction="<%= returnAction %>" />
			</wf:viewControlsHideableBlock>
		</div>
		<div>
			<wf:bulkDeployDefinitionControlHideableBlock hideableBlockId="bulkDeployDefinitionsControl" returnAction="<%= returnAction %>" >
				<wf:bulkDeployDefinitionControl />
			</wf:bulkDeployDefinitionControlHideableBlock>
		</div>
		<wf:deployDefinitionLink forward="deploy_definition" /> 
		<div style="position: absolute; right: 5px; top: 5px;">
			<wf:showDefinitionsHistoryLink forward="show_definitions_history" />
		</div>
	</div>
</wf:listProcessesDefinitionsForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>