<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="ru.runa.common.Version" %>
<%@ page import="ru.runa.common.web.Commons" %>
<%@ page import="ru.runa.common.WebResources" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title></title>
	<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/main.css?"+Version.getHash() %>' />">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/jquery-ui-1.9.2.custom.css" />">
	<script type="text/javascript" src="<html:rewrite page="/js/jquery-1.8.3.min.js" />"></script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.cookie.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery-ui-1.9.2.custom.min.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.ui.mask.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.ui.timepicker.js" />">c=0;</script>
	<% if (!"en".equals(Commons.getLocale(pageContext).getLanguage())) { %>
		<script type="text/javascript" src="/wfe/js/i18n/jquery.ui.datepicker-<%= Commons.getLocale(pageContext).getLanguage() %>.js">c=0;</script>
		<script type="text/javascript" src="/wfe/js/i18n/jquery.ui.timepicker-<%= Commons.getLocale(pageContext).getLanguage() %>.js">c=0;</script>
	<% } %>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/jquery.edit-list.js?"+Version.getHash() %>' />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/common.js?"+Version.getHash() %>' />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.iframe-transport.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.fileupload.js" />">c=0;</script>
	<script type="text/javascript">
		var currentBrowserLanguage = "<%= Commons.getLocale(pageContext).getLanguage() %>";
	</script>
	<script type="text/javascript" src="<html:rewrite page="/js/trumbowyg.js" />" charset="utf-8">c=0;</script>
<% if (!"en".equals(Commons.getLocale(pageContext).getLanguage())) { %>
	<script type="text/javascript" src="/wfe/js/trumbowyg-langs/<%= Commons.getLocale(pageContext).getLanguage() %>.min.js"></script>
<% } %>
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/trumbowyg.css" />">
	<script type="text/javascript" src="<html:rewrite page='<%="/js/taskformutils.js?"+Version.getHash() %>' />"></script>
	<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/fileupload.css?"+Version.getHash() %>' />">
	<% 
		for (String url : WebResources.getTaskFormExternalJsLibs()) {
	%>
		<script type="text/javascript" src="<%= url %>"></script>
	<% 
		}
	%>
	<script type="text/javascript">
	<% 
		String jwt = request.getParameter("jwt");
		long id = Long.valueOf(request.getParameter("id"));
		boolean startForm = Boolean.valueOf(request.getParameter("startForm"));
		String title = request.getParameter("title");
		if (title == null) {
			title = ru.runa.common.web.Commons.getMessage("title.task_form", pageContext);
		}
	%>
	var id = <%= id %>;
	$(document).ready(function () {
		$("<input />", { "type": "hidden", "name": "jwt", "value": "<%= jwt %>"}).appendTo($("#processForm"));
		$("<input />", { "type": "hidden", "name": "startForm", "value": "<%= startForm %>"}).appendTo($("#processForm"));
		$("<input />", { "type": "hidden", "name": "title", "value": "<%= title %>"}).appendTo($("#processForm"));
	});
	</script>
	<script type="text/javascript">$(function(){setFocusOnInvalidInputIfAny()});</script>
</head>
<body>
	<div id="errors">
		<wf:globalExceptions />
		<center>
			<div id="ajaxErrorsDiv" class="errors" style="font-weight: bold; color: #914b98;">
			</div>
			<span class="errors">
				<html:messages id="error">
					<b style="color: red; margin-bottom: 10px; display: block;"><bean:write name="error" /></b>
				</html:messages>
			</span>
		</center>
	</div>
	<div id="content">
		<logic:match parameter="startForm" value="true">
			<wf:startForm title="<%= title %>" definitionId="<%= id %>" action="/submitStartProcessForm" />
		</logic:match>
		<logic:notMatch parameter="startForm" value="true">
			<wf:taskForm title="<%= title %>" taskId="<%= id %>" action="/submitTaskForm" />
		</logic:notMatch>
	</div>
</body>
</html>