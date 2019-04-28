<%@page import="ru.runa.common.Version"%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ page import="ru.runa.wf.web.form.ProcessForm" %>
<%@ page import="ru.runa.common.web.Commons" %>
<%@ page import="ru.runa.common.WebResources" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
<% if (WebResources.isAjaxFileInputEnabled()) { %>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.iframe-transport.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.fileupload.js" />">c=0;</script>
	
<% 
	}
%>
	<script type="text/javascript" src="<html:rewrite page="/js/trumbowyg.js" />" charset="utf-8">c=0;</script>
<% if (!"en".equals(Commons.getLocale(pageContext).getLanguage())) { %>
	<script type="text/javascript" src="/wfe/js/trumbowyg-langs/<%= Commons.getLocale(pageContext).getLanguage() %>.min.js"></script>
<% } %>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/taskformutils.js?"+Version.getHash() %>' />"></script>
	<script type="text/javascript" src="/wfe/js/i18n/delegate.dialog-<%= Commons.getLocale(pageContext).getLanguage() %>.js?<%=Version.getHash()%>">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/delegate.dialog.js?"+Version.getHash() %>' />">c=0;</script>
	<script type="text/javascript">var id = <%= Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME)) %>;</script>
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/trumbowyg.css" />">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/fileupload.css?"+Version.getHash() %>' />">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/delegate.dialog.css?"+Version.getHash() %>' />">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/chatStyles.css?"+Version.getHash() %>' />">
<% 
	for (String url : WebResources.getTaskFormExternalJsLibs()) {
%>
	<script type="text/javascript" src="<%= url %>"></script>
<% 
	}
%>
</tiles:put>
////////////////////////////////////////////////////////
<tiles:put name="body" type="string" >
<%
	long taskId = Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME));
	String title = ru.runa.common.web.Commons.getMessage("title.task_form", pageContext);
%>
<wf:taskDetails batchPresentationId="listTasksForm" title="<%= title %>" taskId="<%= taskId %>" buttonAlignment="right" action="/processTaskAssignment" returnAction="/submitTaskDispatcher.do"/>
<% if (WebResources.isTaskDelegationEnabled()) { %>
	<wf:taskFormDelegationButton taskId="<%= taskId %>" />
<% } %>
<wf:taskForm title="<%= title %>" taskId="<%= taskId %>" action="/submitTaskForm" />

<button id="myBtn">Открыть чат</button>
    <div id="myModal" class="modal" >
        <div class="modal-content" style="width: 336px;position: fixed; top: auto; bottom: 0%; padding-top: 0px; margin-bottom: 0px; height: 496px; display: block; will-change: width, margin-right, right, transform, opacity, left, height; transform: translateY(0%); margin-right: 0px; margin-left: 30px; right: 60px;">
            <div class="modal-header" style="cursor: move">
                <span class="close">&times;</span>
                <button id="btnOp"><img src="/wfe/images/chat_1.png" alt="open" 
          style="vertical-align: middle; width: 12px; height: 12px"></button>
            </div>
            <div class="modal-body">
                <table>
                <tr>
                    <td>name</td>
                    <td>message</td>
                    <td>time</td>
                </tr>
                <tr>
                    <td>name2</td>
                    <td>message2</td>
                    <td>time2</td>
                </tr>
                </table>
            </div>
            <div class="modal-footer">
                <input type="text" name="message"  id="message">
                <button id="btnSend" value="Отправить" onclick="send()">Отправить</button>
            </div>
        </div>
        </div>
        <script type="text/javascript" src="/wfe/js/chatPart1.js"></script>
   <script>
   
   $(document).ready(function() {
	   
	var btn2 = document.getElementById("btnSend");

   btn2.onclick=function send() { 
   var message = document.getElementById("message").value;
   var urlString = "/wfe/ajaxcmd?command=getProcessValue&message="+message;
   var today = new Date();
   var date = today.getFullYear()+'-'+(today.getMonth()+1)+'-'+today.getDate();
   var time = today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();
   var dateTime = date+' '+time;
   var btnOp=document.getElementById("btnOp");
   
		
   $.ajax({
		type: "POST",
		url: urlString,
		dataType: "json",
		contentType: "application/json; charset=UTF-8",
		processData: false,
		success: function(data) {
			 $(".modal-body").append("<table ><tr><td>"+ data.text + "</td></tr><tr><td>"+ dateTime + "</td></tr></table >");
		}
	});
   }
   btnOp.onclick=function(){
	   
	   	$('.modal-content').css({
	   		height:'700px',
	   	    width: '800px',
	   	});
	   	}
	   
   });
   
   </script>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>