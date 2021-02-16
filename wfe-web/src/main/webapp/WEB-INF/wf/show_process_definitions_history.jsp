<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<%@ page import="ru.runa.common.WebResources" %>
<%@ page import="ru.runa.common.web.ProfileHttpSessionHelper" %>
<%@ page import="ru.runa.wfe.presentation.BatchPresentation" %>
<%@ page import="ru.runa.wfe.presentation.filter.StringFilterCriteria" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">

<%
	String returnAction = "/definitions_history.do";
%>
<wf:listDefinitionsHistoryForm batchPresentationId="listProcessesDefinitionsHistoryForm" returnAction="<%= returnAction %>" >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listProcessesDefinitionsHistoryForm" returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listProcessesDefinitionsHistoryForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
</wf:listDefinitionsHistoryForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>