<%@ page import="ru.runa.common.web.Commons" %>
<%@ page import="ru.runa.common.WebResources" %>
<%@ page import="ru.runa.wfe.commons.web.PortletUrlType" %>
<%@ page import="ru.runa.wf.web.MessagesProcesses" %>
<%@ page import="com.google.common.collect.ImmutableMap" %>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<!DOCTYPE html>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <%@include file="/WEB-INF/af/chat_functions.jsp" %>

    <tiles:put name="head" type="string">
        <script type="text/javascript" src="<html:rewrite page="/js/jquery.iframe-transport.js" />">c=0;</script>
        <script type="text/javascript" src="<html:rewrite page="/js/jquery.fileupload.js" />">c=0;</script>
        <script type="text/javascript" src="<html:rewrite page="/js/trumbowyg.js" />" charset="utf-8">c=0;</script>
        <% if (!"en".equals(Commons.getLocale(pageContext).getLanguage())) { %>
        <script type="text/javascript" src="/wfe/js/trumbowyg-langs/<%= Commons.getLocale(pageContext).getLanguage() %>.min.js"></script>
        <% } %>
        <script type="text/javascript" src="<html:rewrite page='<%="/js/taskformutils.js?"+Version.getHash() %>' />"></script>
        <script type="text/javascript" src="<html:rewrite page='<%="/js/updateprocessvariablesutils.js?"+Version.getHash() %>' />">c=0;</script>
        <script type="text/javascript"> var id = <%= getId(request) %>;</script>
        <link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/trumbowyg.css" />">
        <link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/fileupload.css?"+Version.getHash() %>' />">
    </tiles:put>

    <tiles:put name="body" type="string">
        <% Long processId = getId(request); %>
        <table class="box">
            <tr>
                <th class="box">
                    <bean:message key="chat.process.header" /> <%=processId%>
                </th>
            </tr>
            <tr>
                <td align="right">
                    <a href="<%=Commons.getActionUrl(WebResources.ACTION_MAPPING_MANAGE_PROCESS,
                        ImmutableMap.of("id", processId), pageContext, PortletUrlType.Render)%>">
                        <%=MessagesProcesses.TITLE_PROCESS.message(pageContext)%>
                    </a>
                </td>
            </tr>
        </table>
        <wf:processVariableChatMonitor identifiableId='<%= processId %>'/>

        <table class="box">
            <tr>
                <td id="ChatForm" processId="<%= processId %>">
                    <wf:updateProcessVariablesInChat processId='<%= processId %>'/>
                </td>
            </tr>
        </table>
        <wf:chatForm processId='<%= processId %>' title='<%= "" %>'/>
    </tiles:put>

    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>
<%!
    public long getId(HttpServletRequest request) {
        return Long.parseLong(request.getParameter("processId") != null
                ? request.getParameter("processId")
                : request.getParameter("id"));
    }
%>
