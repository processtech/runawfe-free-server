<%@page import="ru.runa.common.Version"%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.wf.web.form.TaskIdForm" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script type="text/javascript">var id = <%= Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME)) %>;</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/updateprocessswimlanesutils.js?"+Version.getHash() %>' />"></script>
</tiles:put>

<tiles:put name="body" type="string" >
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	Long id = Long.parseLong(request.getParameter(parameterName));

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

<wf:updateProcessSwimlanes processId='<%= id %>'/>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>