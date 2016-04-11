<%@ page language="java" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script type="text/javascript" src="<html:rewrite page="/js/xmleditor/codemirror.js" />">c=0;</script> 
	<script type="text/javascript" src="<html:rewrite page="/js/xmleditor.js" />">c=0;</script>
</tiles:put>

	<tiles:put name="body" type="string">
<%
	String parameterName = "botId";
	long id = Long.parseLong(request.getParameter(parameterName));
	String returnAction="/bot.do?" + parameterName+ "=" +id;
	String saveActionUrl = "save_bot.do?id=" + id;
	String createActionUrl = "create_bot_task.do?id=" + id;
%>
        <wf:botTag botId="<%= id %>"/>
        	<table width="100%">
                <tr>
                    <td align="left"><wf:saveBotLink href="<%= saveActionUrl %>"/></td>
                </tr>
            </table>
        <wf:botTaskListTag botId="<%= id %>">
			<table width="100%">
                <tr>
                    <td align="left"><wf:addBotTaskLink href="<%= createActionUrl %>"/></td>
                </tr>
            </table>
        </wf:botTaskListTag>
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>