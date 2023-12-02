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

<wf:processInfoForm identifiableId="<%= processId %>" readOnly="true">
	<table width="100%">
		<tr>
			<td align="right">
				<wf:showProcessLink identifiableId='<%= processId %>' />
			</td>
		</tr>
	</table>
</wf:processInfoForm>

<wf:showTasksHistory identifiableId="<%= processId %>" />	

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>
