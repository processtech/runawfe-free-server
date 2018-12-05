<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<%
	String logDirPath = (String) request.getAttribute("logDirPath");
	String logFileContent = (String) request.getAttribute("logFileContent");
	Object autoReloadTimeoutSec = request.getAttribute("autoReloadTimeoutSec");
	String pagingToolbar = (String) request.getAttribute("pagingToolbar");
%>

<tiles:put name="head" type="string">
	<style>
		.logView {
			border: 1px solid #B6AD84;
			width: 100%;
			min-height: 400px;
		}
		table.log td {
			font-family: Lucida console;
			font-size: 11px;
		}
		table.log td.lineNumbers {
			color: red;
			text-align: right;
			padding-right: 5px;
			border-right: 1px solid #B6AD84;
		}
		table.log td.content {
			white-space: nowrap;
		}
	</style>
	<script>
var autoReloadTimeoutMillis = <%= autoReloadTimeoutSec %> * 1000;
var autoReloadTimer;
$().ready(function() {
	$("input[name='autoReload']").click(function() {
		if ($(this).is(':checked')) {
			initReloadLogTimer();
		} else {
			clearInterval(autoReloadTimer);
		}
	});
	$("input[name='mode']").click(function() {
		syncModeInputs();
	});
	if ($("input[name='autoReload']").is(':checked')) {
		initReloadLogTimer();
	}
	syncModeInputs();
});

function syncModeInputs() {
	if ($("#endMode").is(':checked')) {
		$("input[name='endLines']").removeAttr("disabled");
	} else {
		$("input[name='endLines']").attr("disabled", "true");
	}
	if ($("#searchMode").is(':checked')) {
		$("input[name='search']").removeAttr("disabled");
		$("input[name='searchCaseSensitive']").removeAttr("disabled");
	} else {
		$("input[name='search']").attr("disabled", "true");
		$("input[name='searchCaseSensitive']").attr("disabled", "true");
	}
}

function initReloadLogTimer() {
	autoReloadTimer = setInterval(reloadLogFile, autoReloadTimeoutMillis);
}

function reloadLogFile() {
	window.location.reload();
}

	</script>
</tiles:put>

<tiles:put name="body" type="string" >
    <wf:managePermissionsForm securedObjectType="LOGS">
        <table width="100%">
            <tr>
                <td align="left">
                    <wf:grantPermissionsLink securedObjectType="LOGS"/>
                </td>
            </tr>
        </table>
    </wf:managePermissionsForm>
    <br /><br />
	<b><%= logDirPath %></b><br />
	<wf:viewLogs logDirPath="<%= logDirPath %>" />
	<% if (logFileContent != null) { %>
		<html:form styleId="downloadForm" action="/viewLogs" method="get">
			<html:hidden property="fileName" />
			<input type="hidden" name="mode" value="5" />
			<table class="box"><tr><th class="box">
				<a href="javascript:$('#downloadForm').submit();" style="color: white;"><bean:write name="viewLogForm" property="fileName" /></a> 
				(<bean:message key="label.logs.allLinesCount"/>: <bean:write name="viewLogForm" property="allLinesCount" />)
			</th></tr></table>
		</html:form>
		<html:form action="/viewLogs" method="get">
			<html:hidden property="fileName" />
			<table><tr><td style="width: 400px;">
				<html:radio property="mode" value="1" styleId="pagingMode" />
					<bean:message key="label.logs.listLines"/> (<html:text property="limitLinesCount" disabled="true" size="5" /> <bean:message key="label.logs.lines"/>)
					<br />
				<html:radio property="mode" value="2" styleId="searchMode" />
					<bean:message key="label.logs.search"/> <html:text property="search" />
					<html:checkbox property="searchCaseSensitive" value="true" /><bean:message key="label.logs.searchCaseSensitive"/>
					<br />
				<html:radio property="mode" value="3" styleId="endMode" />
					<bean:message key="label.logs.showLast"/> <html:text property="endLines" size="5" /> <bean:message key="label.logs.lines"/>
					<br />
			</td><td>
				<html:radio property="mode" value="4" styleId="searchErrorsMode" />
					<bean:message key="label.logs.searchErrors"/>
					<br />
				<html:radio property="mode" value="6" styleId="searchWarnsMode" />
					<bean:message key="label.logs.searchWarns"/>
					<br />
				<html:checkbox property="autoReload" value="true" /><bean:message key="label.logs.autoReload"/> <%= autoReloadTimeoutSec %> <bean:message key="label.logs.seconds"/>
				<br />
				<html:submit><bean:message key="button.form"/></html:submit>
			</td></tr></table>
		</html:form>
		<% if (pagingToolbar != null) {out.println(pagingToolbar);} %>
		<div class="logView">
			<%= logFileContent %>
		</div>
		<% if (pagingToolbar != null) {out.println(pagingToolbar);} %>
	<% } %>
 </tiles:put>
 
<tiles:put name="messages" value="../common/messages.jsp" />

</tiles:insert>