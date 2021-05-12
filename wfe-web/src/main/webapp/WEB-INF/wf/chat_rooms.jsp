<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<!DOCTYPE html>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

    <tiles:put name="body" type="string">
        <%
            String returnAction = "/chat_rooms.do";
            String batchPresentationId = "listChatRoomsForm";
        %>
        <wf:listChatRoomsForm batchPresentationId="<%= batchPresentationId %>" returnAction="<%= returnAction %>">
            <div style="position: relative;">
                <wf:viewControlsHideableBlock hideableBlockId="<%= batchPresentationId %>" returnAction="<%= returnAction %>">
                    <wf:tableViewSetupForm batchPresentationId="<%= batchPresentationId %>" returnAction="<%= returnAction %>"/>
                </wf:viewControlsHideableBlock>
            </div>
        </wf:listChatRoomsForm>
    </tiles:put>

    <tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>
