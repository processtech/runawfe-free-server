<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <%@include file="/WEB-INF/af/chat_notifications.jsp" %>

    <tiles:put name="body" type="string">
        <%
            String batchPresentationId = "listChatRoomsForm";
        %>
        <wf:listChatRoomsForm batchPresentationId="<%= batchPresentationId %>"/>
    </tiles:put>

    <tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>