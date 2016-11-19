<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	Long processId = Long.parseLong(request.getParameter(parameterName));
%>

<wf:showTasksHistory identifiableId="<%= processId %>" >	
<table width="100%">
	<tr>
		<td align="right">
			<wf:updateProcessLink identifiableId='<%= processId %>' href='<%= "/manage_process.do?" + parameterName+ "=" + processId %>'  />
		</td>
	</tr>
</table> 
</wf:showTasksHistory>


</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>
