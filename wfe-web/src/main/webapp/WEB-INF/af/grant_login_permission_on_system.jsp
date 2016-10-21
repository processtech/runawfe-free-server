<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String returnAction = "/grant_login_permission_on_system.do";
%>
<wf:listExecutorsWithoutPermissionsOnSystemForm batchPresentationId="listExecutorsWithoutPermissionsOnSystemForm"  returnAction="<%= returnAction %>" >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listExecutorsWithoutPermissionsOnSystemForm"  returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listExecutorsWithoutPermissionsOnSystemForm"  returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
</wf:listExecutorsWithoutPermissionsOnSystemForm>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>