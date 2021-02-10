<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
	<%@include file="/WEB-INF/af/chat_notifications.jsp" %>

<tiles:put name="body" type="string">
<%
	long relationId = Long.parseLong(request.getParameter("relationId"));
	String returnAction = "/manage_relation.do?relationId=" + relationId;
%>
<wf:relationForm relationId="<%= relationId %>" />
<wf:listRelationPairsForm batchPresentationId="listRelationPairs" buttonAlignment="right" returnAction="<%= returnAction %>" relationId="<%= relationId %>">
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listRelationPairs" returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listRelationPairs" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
	<table width="100%">
		<tr>
			<td align="left">
				<wf:createRelationPairLink relationId="<%= relationId %>"/>
			</td>
			<td align="right">
				<wf:managePermissionsLink securedObjectType="RELATION" identifiableId="<%= relationId %>" />
			</td>
		</tr>
	</table>
</wf:listRelationPairsForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>