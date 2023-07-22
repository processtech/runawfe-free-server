<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
	<p>Версия <b><%= ru.runa.common.Version.get() %> Free</b></p>
	<p>Запуск <b><%= ru.runa.common.Version.getStartupDateTimeString() %></b></p>
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />

</tiles:insert>