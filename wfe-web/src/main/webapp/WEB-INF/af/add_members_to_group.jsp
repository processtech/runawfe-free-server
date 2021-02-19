<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
<tiles:put name="body" type="string" >
<%
	String executorIdParam= IdForm.ID_INPUT_NAME;
	long executorIdValue = Long.parseLong(request.getParameter(executorIdParam));
	String returnAction = "/add_members_to_group.do?" + executorIdParam+ "=" +executorIdValue;
%>
	<wf:listNotGroupMembersForm batchPresentationId="listNotGroupMembersForm"  identifiableId="<%= executorIdValue %>"  returnAction='<%= returnAction %>' >
		<div>
			<wf:viewControlsHideableBlock hideableBlockId="listNotGroupMembersForm" returnAction='<%= returnAction %>' >
				<wf:tableViewSetupForm batchPresentationId="listNotGroupMembersForm" action="/tableViewSetup" returnAction='<%= returnAction %>'  />
			</wf:viewControlsHideableBlock>
		</div>
	</wf:listNotGroupMembersForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>