<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">
<%
	String returnAction = "/manage_archived_processes.do";
	String batchPresentationId = "listArchivedProcessesForm";
%>
<wf:listProcessesForm batchPresentationId="<%= batchPresentationId %>" returnAction="<%= returnAction %>">
    <div style="position: relative;">
        <div>
            <wf:viewControlsHideableBlock hideableBlockId="<%= batchPresentationId %>"  returnAction="<%= returnAction %>">
                <wf:tableViewSetupForm batchPresentationId="<%= batchPresentationId %>" returnAction="<%= returnAction %>" excelExportAction="/exportExcelProcesses" />
            </wf:viewControlsHideableBlock>
        </div>
        <div style="position: absolute; right: 5px; top: 5px;">
            <wf:managePermissionsLink securedObjectType="PROCESSES" />
        </div>
    </div>
</wf:listProcessesForm>
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>