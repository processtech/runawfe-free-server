<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
    long relationId = Long.parseLong(request.getParameter("id"));
	String returnAction = "/grant_permissions_on_relation.do?id=" + relationId;
%>
<wf:listExecutorsWithoutPermissionsOnRelationForm batchPresentationId="listExecutorsWithoutPermissionsOnRelationForm" returnAction="<%= returnAction %>" identifiableId="<%= relationId %>">
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listExecutorsWithoutPermissionsOnRelationForm" returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listExecutorsWithoutPermissionsOnRelationForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
</wf:listExecutorsWithoutPermissionsOnRelationForm>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>