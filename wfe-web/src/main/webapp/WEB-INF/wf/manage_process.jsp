<%@ page pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.Version"%>
<%@ page import="ru.runa.common.web.Commons"%>
<%@ page import="ru.runa.wf.web.form.TaskIdForm" %>
<%@ page import="ru.runa.wf.web.action.ShowGraphModeHelper" %>
<%@ page import="ru.runa.common.WebResources" %>

<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
<script type="text/javascript" src="<html:rewrite page='<%="/js/errorviewer.js?"+Version.getHash() %>' />">c=0;</script>
<script type="text/javascript" src="<html:rewrite page='<%="/js/processgraphutils.js?"+Version.getHash() %>' />">c=0;</script>
<script type="text/javascript" src="/wfe/js/i18n/processupgrade.dialog-<%= Commons.getLocale(pageContext).getLanguage() %>.js">c=0;</script>
<script type="text/javascript" src="<html:rewrite page='<%="/js/processupgrade.dialog.js?"+Version.getHash() %>' />">c=0;</script>
<script type="text/javascript">
$(document).ready(function() {
	/
	var hash = window.location.hash.substr(1);
	var href = $('#nav td a').each(function(){
		var href = $(this).attr('href');
		if(hash==href.substr(0,href.length-5)){
			var toLoad = hash+'.html #content';
			$('#content').load(toLoad)
		}											
	});
	/
	
	$('#nav td a').click(function(){
		var toLoad = $(this).attr('href')+' #content';
		$('#content').hide('fast',loadContent);
		//$('#load').remove();
		//$('#wrapper').append('<span id="load">LOADING...</span>');
		//$('#load').fadeIn('normal');
		
		function loadContent() {
			$('#content').load(toLoad,'',showNewContent())
		}
		function showNewContent() {
			$('#content').show('normal',hideLoader());
		}
		function hideLoader() {
			$('#load').fadeOut('normal');
		}
		return false;
	});
});
</script>
<% if (WebResources.getDiagramRefreshInterval() > 0) { %>
<script type="text/javascript">
$(window).load(function() {
  window.setInterval("Reload()", <%= WebResources.getDiagramRefreshInterval() %>*1000);
});
function Reload() { 
   var src = $("#graph").attr("src");
   var pos = src.indexOf('timestamp');
   if (pos >= 0) {
      src = src.substr(0, pos);
   } else {
      src = src + '&';
   }
   src = src + "timestamp=" + new Date().getTime();
   $("#graph").attr("src", src);
}  
</script>
<% } %>
</tiles:put>

<tiles:put name="body" type="string">
<%
	long id = Long.parseLong(request.getParameter("id"));
	Long taskId = null;
	String taskIdString = request.getParameter(TaskIdForm.TASK_ID_INPUT_NAME);
	if (taskIdString != null && !"null".equals(taskIdString)) {
		taskId = Long.parseLong(taskIdString);
	}
	Long childProcessId = null;
	String childProcessIdString = request.getParameter("childProcessId");
	if (childProcessIdString != null && !"null".equals(childProcessIdString)) {
		childProcessId = Long.parseLong(childProcessIdString);
	}
	boolean flagOpen=Boolean.parseBoolean(request.getParameter("flagOpen"));
	boolean graphMode = ShowGraphModeHelper.isShowGraphMode();
%>
<wf:processInfoForm buttonAlignment="right" identifiableId='<%= id %>' taskId='<%= taskId %>'>
<table width="100%">
	<tr>
		<td align="right">
		<% if(graphMode) { %>
			<wf:showProcessGraphLink identifiableId='<%=id %>' href='<%= "/show_process_graph.do?id=" + id + "&taskId=" + taskId + "&childProcessId=" + childProcessId %>'  />
		<% } %>
		</td>
		<td width="200" align="right">
			<wf:showTasksHistoryLink identifiableId='<%=id %>' href='<%= "/show_tasks_history.do?id=" + id %>'  />
		</td>
	</tr>
	<tr>
		<td align="right">
			<wf:showHistoryLink identifiableId='<%=id %>' href='<%= "/show_history.do?id=" + id %>'  />
		</td>
		<td width="200" align="right">
			<wf:showGanttDiagramLink identifiableId='<%=id %>' href='<%= "/show_gantt_diagram.do?id=" + id %>'  />
		</td>
	</tr>
	<tr>
		<td align="right">
			<wf:showGraphHistoryLink identifiableId='<%=id %>' href='<%= "/show_graph_history.do?id=" + id %>'  />
		</td>
		<td width="200" align="right">
			<wf:managePermissionsLink securedObjectType="PROCESS" identifiableId="<%= id %>"  />
		</td>
	</tr>
</table>
   <button id="myBtn">Открыть чат</button>
    <div id="myModal" class="modal" >
        <div class="modal-content" style="width: 336px;position: fixed; top: auto; bottom: 0%; padding-top: 0px; margin-bottom: 0px; height: 496px; display: block; will-change: width, margin-right, right, transform, opacity, left, height; transform: translateY(0%); margin-right: 0px; margin-left: 30px; right: 60px;">
            <div class="modal-header" style="cursor: move">
                <span class="close">&times;</span>
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
</wf:processInfoForm>

<wf:processActiveTaskMonitor identifiableId='<%= id %>' />
<wf:processSwimlaneMonitor identifiableId='<%= id %>' />
<wf:processVariableMonitor identifiableId='<%= id %>' flagOpen='<%=flagOpen %>' />
<% if(!graphMode) { %>
	<wf:processGraphForm identifiableId='<%= id %>' taskId='<%= taskId %>' childProcessId='<%= childProcessId %>'/>
<% } %>
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>