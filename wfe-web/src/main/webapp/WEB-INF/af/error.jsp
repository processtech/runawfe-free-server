<%@ page language="java" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <%@include file="/WEB-INF/af/chat_notifications.jsp" %>

<tiles:put name="body" type="string">
<span class="errors"><bean:message key="title.errors"/></span>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>