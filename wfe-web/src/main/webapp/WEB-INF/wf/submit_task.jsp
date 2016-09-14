<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.wf.web.form.TaskIdForm" %>
<%@ page import="ru.runa.wf.web.form.ProcessForm" %>
<%@ page import="ru.runa.common.WebResources" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
<% if (WebResources.isAjaxFileInputEnabled()) { %>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.iframe-transport.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.fileupload.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/taskformutils.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/delegate.dialog.js" />">c=0;</script>
	<script type="text/javascript">var id = <%= Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME)) %>;</script>
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/fileupload.css" />">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/delegate.dialog.css" />">
<% 
   }
   for (String url : WebResources.getTaskFormExternalJsLibs()) {
%>
	<script type="text/javascript" src="<%= url %>"></script>
<% 
   }
%>
</tiles:put>

<tiles:put name="body" type="string" >
<%
	long taskId = Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME));
	boolean isDelegationPermitted = Boolean.parseBoolean(request.getParameter(TaskIdForm.DELEGATION_PERMITTED));
	long actorId =  Long.parseLong(request.getParameter(ProcessForm.ACTOR_ID_INPUT_NAME));
	String title = ru.runa.common.web.Commons.getMessage("title.task_form", pageContext);
%>
<wf:taskDetails batchPresentationId="listTasksForm" title="<%= title %>" taskId="<%= taskId %>" actorId="<%= actorId %>" buttonAlignment="right" action="/processTaskAssignment" returnAction="/submitTaskDispatcher.do"/>
<% if (WebResources.isTaskDelegationEnabled() && isDelegationPermitted) { %>
	<wf:taskFormDelegationButton taskId="<%= taskId %>" />
<% } %>
<wf:taskForm title="<%= title %>" taskId="<%= taskId %>" actorId="<%= actorId %>" action="/submitTaskForm" />

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>