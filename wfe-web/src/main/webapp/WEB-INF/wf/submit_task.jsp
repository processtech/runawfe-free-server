<%@page import="ru.runa.wfe.service.delegate.Delegates"%>
<%@page import="ru.runa.common.Version"%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.wf.web.form.ProcessForm" %>
<%@ page import="ru.runa.common.web.Commons" %>
<%@ page import="ru.runa.common.WebResources" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
<% if (WebResources.isAjaxFileInputEnabled()) { %>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.iframe-transport.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.fileupload.js" />">c=0;</script>
<% 
	}
%>
	<script type="text/javascript" src="<html:rewrite page="/js/trumbowyg.js" />" charset="utf-8">c=0;</script>
<% if (!"en".equals(Commons.getLocale(pageContext).getLanguage())) { %>
	<script type="text/javascript" src="/wfe/js/trumbowyg-langs/<%= Commons.getLocale(pageContext).getLanguage() %>.min.js"></script>
<% } %>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/taskformutils.js?"+Version.getHash() %>' />"></script>
	<script type="text/javascript" src="/wfe/js/i18n/delegate.dialog-<%= Commons.getLocale(pageContext).getLanguage() %>.js?<%=Version.getHash()%>">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/delegate.dialog.js?"+Version.getHash() %>' />">c=0;</script>
	<script type="text/javascript">var id = <%= Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME)) %>;</script>
	<script type="text/javascript">$(function(){setFocusOnInvalidInputIfAny()});</script>
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/trumbowyg.css" />">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/fileupload.css?"+Version.getHash() %>' />">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/delegate.dialog.css?"+Version.getHash() %>' />">
<% 
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
	String title = ru.runa.common.web.Commons.getMessage("title.task_form", pageContext);
%>
<wf:taskDetails batchPresentationId="listTasksForm" title="<%= title %>" taskId="<%= taskId %>" buttonAlignment="right" action="/processTaskAssignment" returnAction="/submitTaskDispatcher.do"/>
<% if(WebResources.isChatEnabled()){%>
<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/chat.css?"+Version.getHash() %>' />">
<script type="text/javascript" src="/wfe/js/chat.js"></script>
<div style="float:left; max-width: 150px; margin-top: -25px;">
	<a id="openChatButton" onclick="openChat()">Открыть чат <span id="countNewMessages" class="countNewMessages" title="Непрочитанные">0</span></a>
</div>
<div id="ChatForm"  processId="<%= Delegates.getTaskService().getTask(Commons.getUser(request.getSession()), taskId).getProcessId() %>"></div>
<% }%>
<% if (WebResources.isTaskDelegationEnabled()) { %>
	<wf:taskFormDelegationButton taskId="<%= taskId %>" />
<% } %>

<wf:taskForm title="<%= title %>" taskId="<%= taskId %>" action="/submitTaskForm" />
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>