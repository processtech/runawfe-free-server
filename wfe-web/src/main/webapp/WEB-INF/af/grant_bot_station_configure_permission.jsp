<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <tiles:put name="body" type="string">
        <% String returnAction = "/grant_bot_station_permission.do"; %>
        <wf:ListExecutorsWithoutPermissionsOnBotStationFormTag
                batchPresentationId="listExecutorsWithoutBotStationPermission"
                returnAction="<%= returnAction %>">
            <div>
                <wf:viewControlsHideableBlock hideableBlockId="listExecutorsWithoutBotStationPermission"
                                              returnAction="<%= returnAction %>">
                    <wf:tableViewSetupForm batchPresentationId="listExecutorsWithoutBotStationPermission"
                                           returnAction="<%= returnAction %>"/>
                </wf:viewControlsHideableBlock>
            </div>
        </wf:ListExecutorsWithoutPermissionsOnBotStationFormTag>
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>