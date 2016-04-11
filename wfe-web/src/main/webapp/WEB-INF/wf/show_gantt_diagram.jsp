<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
<%@page import="ru.runa.common.web.Commons"%>
<%@ page import="ru.runa.common.web.form.IdForm" %>

<tiles:put name="head" type="string">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/gantt.css" />" />
	<script language="javascript" src="<html:rewrite page="/js/gantt.js" />"></script>
</tiles:put>

<tiles:put name="body" type="string">
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	Long processId = Long.parseLong(request.getParameter(parameterName));
%>

<wf:showGanttDiagram identifiableId="<%= processId %>" >
<table width="100%">
	<tr>
		<td align="right">
			<wf:updateProcessLink identifiableId='<%=processId %>' href='<%= "/manage_process.do?" + parameterName+ "=" + processId %>'  />
		</td>
	</tr>
</table>
<div style="position:relative" class="gantt" id="GanttChartDIV"></div>
<script language="javascript">
	var g = new JSGantt.GanttChart('g', document.getElementById('GanttChartDIV'), 'day');
	g.setShowRes(1); // Show/Hide Responsible (0/1)
	g.setShowDur(1); // Show/Hide Duration (0/1)
	g.setShowComp(0); // Show/Hide % Complete(0/1)
	g.setShowStartDate(1); // Show/Hide Start Date(0/1)
	g.setShowEndDate(1); // Show/Hide End Date(0/1)
</script>
</wf:showGanttDiagram>

<script language="javascript">
	g.Draw();       
	g.DrawDependencies();
</script>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>
