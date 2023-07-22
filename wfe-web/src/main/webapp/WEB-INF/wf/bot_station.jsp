<%@page import="ru.runa.common.Version"%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.common.WebResources" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
	<tiles:put name="head" type="string">
		<% if (WebResources.isBulkDeploymentElements()) { %>
			<script type="text/javascript" src="<html:rewrite page="/js/jquery.iframe-transport.js" />">c=0;</script>
			<script type="text/javascript" src="<html:rewrite page="/js/jquery.fileupload.js" />">c=0;</script>
			<script type="text/javascript" src="<html:rewrite page='<%="/js/bulkuploadutils.js?"+Version.getHash() %>' />">c=0;</script>
			<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/fileupload.css?"+Version.getHash() %>' />">
		<% } %>
	</tiles:put>
	<tiles:put name="body" type="string">
<%
	long botStationId = Long.parseLong(request.getParameter("botStationId"));
	String saveActionUrl = "save_bot_station.do?id=" + botStationId;
%>
        <wf:botStationTag botStationId="<%= botStationId %>"/>
        <table width="100%">
            <tr>
                <td align="left"><wf:saveBotStationLink href="<%= saveActionUrl %>" /></td>
            </tr>
        </table>
        <wf:botStationStatusTag botStationId="<%= botStationId %>"/>

        <wf:deployBot botStationId="<%= botStationId %>"/>
        <wf:botListTag botStationId="<%= botStationId %>">
        </wf:botListTag>
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>