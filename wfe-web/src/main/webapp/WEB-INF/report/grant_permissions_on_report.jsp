<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<%@ page import="ru.runa.common.web.form.IdForm" %>

<tiles:put name="body" type="string" >
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	long id = Long.parseLong(request.getParameter(parameterName));
	String returnAction = "/grant_permissions_on_report.do?" + parameterName + "=" + id;
%>
<wf:listExecutorsWithoutPermissionsOnReportForm batchPresentationId="listExecutorsWithoutPermissionsOnReportsForm" returnAction="<%= returnAction %>" identifiableId='<%=id %>' >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listExecutorsWithoutPermissionsOnReportsForm" returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listExecutorsWithoutPermissionsOnReportsForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
</wf:listExecutorsWithoutPermissionsOnReportForm>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>