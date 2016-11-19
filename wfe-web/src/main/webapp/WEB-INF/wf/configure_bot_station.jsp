<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <tiles:put name="body" type="string">
	    <wf:deployBotStation />
        <wf:botStationList buttonAlignment="right">
            <table width="100%">
                <tr>
                    <td align="left"><wf:createBotStationLink href="add_bot_station.do"/></td>
                    <td align="right">
                        <wf:grantBotStationConfigurePermissionLink forward="bot_station_permission"/>
                    </td>
                </tr>
            </table>
        </wf:botStationList>
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>