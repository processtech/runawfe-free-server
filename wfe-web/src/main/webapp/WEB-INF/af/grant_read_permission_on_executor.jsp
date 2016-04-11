<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String executorIdParam= IdForm.ID_INPUT_NAME;
	long executorIdValue = Long.parseLong(request.getParameter(executorIdParam));
	String returnAction = "/grant_read_permission_on_executor.do?" + executorIdParam+ "=" +executorIdValue;
%>
	<wf:listExecutorsWithoutPermissionsOnExecutorForm  batchPresentationId="listExecutorsWithoutPermissionsOnExecutorForm" identifiableId="<%= executorIdValue %>" returnAction="<%= returnAction %>" >
		<div>
			<wf:viewControlsHideableBlock hideableBlockId="listExecutorsWithoutPermissionsOnExecutorForm"  returnAction="<%= returnAction %>" >
				<wf:tableViewSetupForm batchPresentationId="listExecutorsWithoutPermissionsOnExecutorForm" returnAction="<%= returnAction %>" />
			</wf:viewControlsHideableBlock>
		</div>
	</wf:listExecutorsWithoutPermissionsOnExecutorForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>