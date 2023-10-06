<%@ page pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	long id = Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME));
%>

<wf:processInfoForm identifiableId="<%= id %>" readOnly="true">
	<table width="100%">
		<tr>
			<td align="right">
				<wf:showProcessLink identifiableId='<%= id %>' />
			</td>
		</tr>
	</table>
</wf:processInfoForm>

<wf:showHistory identifiableId="<%= id %>" />

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>