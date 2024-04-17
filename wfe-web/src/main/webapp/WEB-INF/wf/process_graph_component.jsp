<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.wf.web.form.TaskIdForm" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<%
	String parameterName = IdForm.ID_INPUT_NAME;
	Long id = Long.parseLong(request.getParameter(parameterName));
	Long taskId = null;
	String taskIdString = request.getParameter(TaskIdForm.TASK_ID_INPUT_NAME);
	if (taskIdString != null && !"null".equals(taskIdString)) {
		taskId = Long.parseLong(taskIdString);
	}
	Long childProcessId = null;
	String childProcessIdString = request.getParameter("childProcessId");
	if (childProcessIdString != null && !"null".equals(childProcessIdString)) {
		childProcessId = Long.parseLong(childProcessIdString);
	}
	String subprocessId = request.getParameter("subprocessId");
	String graphMode = request.getParameter("graphMode");
%>

<wf:processGraphForm identifiableId='<%= id %>' taskId='<%= taskId %>' childProcessId='<%= childProcessId %>' subprocessId='<%= subprocessId %>' graphMode='<%= graphMode %>'>
</wf:processGraphForm>
