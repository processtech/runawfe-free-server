<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script type="text/javascript" src="<html:rewrite page="/js/processgraphutils.js" />">c=0;</script>
</tiles:put>

<tiles:put name="body" type="string" >
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	long id = Long.parseLong(request.getParameter(parameterName));
%>

<wf:showGraphHistoryForm identifiableId="<%= id %>" >
<table width="100%">
	<tr>
		<td align="right">
			<wf:updateProcessLink identifiableId='<%=id %>' href='<%= "/manage_process.do?" + parameterName+ "=" + id %>'  />
		</td>
	</tr>
</table>
	 
</wf:showGraphHistoryForm>


</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>