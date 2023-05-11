<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.af.web.form.CreateDigitalSignatureForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
    <%
	    long id = Long.parseLong(request.getParameter(CreateDigitalSignatureForm.EXECUTOR_ID_INPUT_NAME));
    %>
	<wf:createDigitalSignatureForm identifiableId= "<%= id %>" />
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>
