<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <%@include file="/WEB-INF/af/chat_notifications.jsp" %>

<tiles:put name="body" type="string">
<%
    String returnAction = "/show_system_logs.do";
%>
<wf:showSystemLogForm batchPresentationId="listSystemLogsForm" returnAction="<%= returnAction %>">
    <div>
        <wf:viewControlsHideableBlock hideableBlockId="listSystemLogsForm"  returnAction="<%= returnAction %>" >
            <wf:tableViewSetupForm batchPresentationId="listSystemLogsForm" returnAction="<%= returnAction %>" />
        </wf:viewControlsHideableBlock>
    </div>
</wf:showSystemLogForm>
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>