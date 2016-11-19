<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	long id = Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME));
%>

<wf:showHistory identifiableId="<%= id %>" >
<table width="100%">
	<tr>
		<td align="right">
			<wf:updateProcessLink identifiableId='<%=id %>' href='<%= "/manage_process.do?" + IdForm.ID_INPUT_NAME + "=" + id %>'  />
		</td>
	</tr>
</table>
</wf:showHistory>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>