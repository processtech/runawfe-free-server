<%@ page import="ru.runa.common.web.Commons" %>
<%@ page pageEncoding="UTF-8" %>
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
        <% String title = "Чат процесса " + processId; %>
        <table width="100%">
            <tr>
                <td align="right">
                    <a href="<%= "/wfe/manage_process.do?id=" + processId %>">Экземпляр процесса</a>
                </td>
            </tr>
        </table>
        <wf:processVariableChatMonitor identifiableId='<%= processId %>'/>

        <div id="ChatForm" processId="<%= processId %>">
            <wf:updateProcessVariablesInChat processId='<%= processId %>'/>
        </div>

        <wf:chatForm processId='<%= processId %>' title='<%= title %>'/>
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
