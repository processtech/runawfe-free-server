<%@ page language="java" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
	<tiles:put name="body" type="string">
<%
	long id = Long.parseLong(request.getParameter("botStationId"));
%>
        <wf:addBotTag botStationId="<%= id %>"/>
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>