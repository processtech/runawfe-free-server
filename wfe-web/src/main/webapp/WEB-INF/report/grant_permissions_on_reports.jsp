<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String returnAction = "/grant_permissions_on_reports.do";
%>
<wf:listExecutorsWithoutPermissionsOnReportsForm batchPresentationId="listExecutorsWithoutPermissionsOnReportsForm" returnAction="<%= returnAction %>" >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listExecutorsWithoutPermissionsOnReportsForm" returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listExecutorsWithoutPermissionsOnReportsForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
</wf:listExecutorsWithoutPermissionsOnReportsForm>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>