<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">

<%
	String returnAction = "/definitions_history.do";
%>
<wf:listDefinitionsHistoryForm  batchPresentationId="listProcessesDefinitionsHistoryForm" returnAction="<%= returnAction %>" >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listProcessesDefinitionsHistoryForm" returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listProcessesDefinitionsHistoryForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
</wf:listDefinitionsHistoryForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>