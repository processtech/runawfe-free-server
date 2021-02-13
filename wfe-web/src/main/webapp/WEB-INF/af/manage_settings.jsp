<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ page import="ru.runa.common.web.Commons" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
	<%@include file="/WEB-INF/af/chat_notifications.jsp" %>
<tiles:put name="body" type="string" >
<wf:systemSettings>
	<div align="right" <%= Commons.isDatabaseSettingsEnabled() %>>
		<a href="drop_settings.do" onclick="return confirm('<%= Commons.getMessage("confirmpopup.drop.settings", pageContext) %>')" >
			<%= Commons.getMessage("link.drop_settings", pageContext) %>
		</a>
	</div>
</wf:systemSettings>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>