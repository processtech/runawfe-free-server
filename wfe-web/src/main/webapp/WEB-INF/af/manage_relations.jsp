<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">
<%
	String returnAction = "/manage_relations.do";
%>
<wf:listRelationsForm batchPresentationId="listRelations" buttonAlignment="right" returnAction="<%= returnAction %>">
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listRelations" returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listRelations" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
	<table width="100%">
		<tr>
			<td align="left">
				<wf:createRelationLink />
			</td>
			<td align="right">
				<wf:managePermissionsOnRelationGroupLink />
			</td>
		</tr>
	</table>
</wf:listRelationsForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>