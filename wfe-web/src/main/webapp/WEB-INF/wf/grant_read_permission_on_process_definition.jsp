<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String definitionIdParam= IdForm.ID_INPUT_NAME;
	long definitionIdValue = Long.parseLong(request.getParameter(definitionIdParam));
	String returnAction = "/grant_read_permission_on_process_definition.do?" + definitionIdParam+ "=" +definitionIdValue;
%>
	<wf:listExecutorsWithoutPermissionsOnDefinitionForm batchPresentationId="listExecutorsWithoutPermissionsOnDefinitionForm" identifiableId ="<%= definitionIdValue %>"  returnAction="<%= returnAction %>" >
		<div>
			<wf:viewControlsHideableBlock hideableBlockId="listExecutorsWithoutPermissionsOnDefinitionForm"  returnAction="<%= returnAction %>" >
				<wf:tableViewSetupForm batchPresentationId="listExecutorsWithoutPermissionsOnDefinitionForm" returnAction="<%= returnAction %>" />
			</wf:viewControlsHideableBlock>
		</div>
	</wf:listExecutorsWithoutPermissionsOnDefinitionForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>