<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <%@include file="/WEB-INF/af/chat_view.jsp" %>

    <tiles:put name="body" type="string">
        <% Long processId = Long.parseLong(request.getParameter("processId")); %>
        <% String title = "Чат процесса " + processId; %>

        <wf:processInfoForm identifiableId='<%= processId %>'/>

        <div id="ChatForm" processId="<%= processId %>"></div>

        <wf:chatForm processId='<%= processId %>' title='<%= title %>'/>
    </tiles:put>

    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>