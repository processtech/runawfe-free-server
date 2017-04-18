<%@page import="ru.runa.wfe.commons.GitProperties"%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.wf.web.form.TaskIdForm" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.iframe-transport.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.fileupload.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/taskformutils.js?"+GitProperties.getCommit() %>' />"></script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/updateprocessvariablesutils.js?"+GitProperties.getCommit() %>' />">c=0;</script>	
	<script type="text/javascript">var id = <%= Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME)) %>;</script>
	<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/fileupload.css?"+GitProperties.getCommit() %>' />">
</tiles:put>

<tiles:put name="body" type="string" >
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	Long id = Long.parseLong(request.getParameter(parameterName));

%>

<wf:updateProcessVariables processId='<%= id %>'/>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>