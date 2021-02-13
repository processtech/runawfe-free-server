<%@page import="ru.runa.common.Version" %>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <%@include file="/WEB-INF/af/chat_view.jsp" %>

    <tiles:put name="body" type="string">
        <% Long processId = Long.parseLong(request.getParameter("processId")); %>

        <wf:processInfoForm identifiableId='<%= processId %>' />

        <link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/chat.css?"+Version.getHash() %>' />">
        <div style="float:right; max-width: 150px; margin-top: -25px;">
            <a id="openChatButton" onclick="openChat()"><span id="openChatButtonText"></span>
                <span id="countNewMessages" class="countNewMessages" title="Непрочитанные">0</span></a>
        </div>
        <div id="ChatForm" processId="<%= processId %>"></div>

        <wf:chatForm batchPresentationId="listTasksForm" />
    </tiles:put>

    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>