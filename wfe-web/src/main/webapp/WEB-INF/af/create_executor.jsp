<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.af.web.form.CreateExecutorForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
	<%@include file="/WEB-INF/af/chat_notifications.jsp" %>

<tiles:put name="body" type="string" >
<% 
	String executorType = request.getParameter(CreateExecutorForm.EXECUTOR_TYPE_INPUT_NAME);
%>
	<wf:createExecutorForm type="<%= executorType %>" />
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>