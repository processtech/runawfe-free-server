<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">
<%
	String returnAction = "/manage_processes.do";
%>

 <table width="100%">
            <tr>
                <td align="left">
                    <a href="<c:url value='/manage_suspended_processes.do'/>">
                        <bean:message key="tab.processes.suspended"/>
                    </a>
                </td>
            </tr>
        </table>


<wf:listProcessesForm  batchPresentationId="listProcessesForm"  returnAction="<%= returnAction %>">
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listProcessesForm"  returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listProcessesForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
</wf:listProcessesForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>