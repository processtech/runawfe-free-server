<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String parameterName= IdForm.ID_INPUT_NAME;
	long id = Long.parseLong(request.getParameter(parameterName));
%>

<wf:updatePermissionsOnReportForm  identifiableId="<%= id %>"  >
<table width="100%">
	<tr>
		<td align="left">
			<wf:grantPermissionsOnReportLink identifiableId='<%=id %>' href='<%= "/grant_permissions_on_report.do?" + parameterName+ "=" + id %>'  />
		</td>
	<tr>
</table>
</wf:updatePermissionsOnReportForm>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>