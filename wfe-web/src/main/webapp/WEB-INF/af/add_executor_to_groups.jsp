<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
<tiles:put name="body" type="string" >
<%
	String executorIdParam= IdForm.ID_INPUT_NAME;
	long executorIdValue = Long.parseLong(request.getParameter(executorIdParam));
	String returnAction = "/add_executor_to_groups.do?" + executorIdParam+ "=" +executorIdValue;
%>
	<wf:listNotExecutorGroupsForm batchPresentationId="listNotExecutorGroupsForm" identifiableId="<%= executorIdValue %>" returnAction="<%= returnAction %>" >
		<div>
			<wf:viewControlsHideableBlock hideableBlockId="listNotExecutorGroupsForm" returnAction="<%= returnAction %>" >
				<wf:tableViewSetupForm batchPresentationId="listNotExecutorGroupsForm" action="/tableViewSetup" returnAction="<%= returnAction %>" />
			</wf:viewControlsHideableBlock>
		</div>
	</wf:listNotExecutorGroupsForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>