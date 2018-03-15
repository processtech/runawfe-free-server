<%@page import="ru.runa.common.Version"%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script>
	var storageVisible = true;
	var systemErrorsVisible = true;
	var processErrorsVisible = true;
	$(document).ready(function() {
		$("#storageButton").click(function() {
			if (storageVisible) {
				$("#storageContentDiv").hide();
				$("#storageImg").attr("src", "/wfe/images/view_setup_hidden.gif");
				storageVisible = false;
			} else {
				$("#storageContentDiv").show();
				$("#storageImg").attr("src", "/wfe/images/view_setup_visible.gif");
				storageVisible = true;
			}
		});
		$("#systemErrorsButton").click(function() {
			if (systemErrorsVisible) {
				$("#systemErrorsContentDiv").hide();
				$("#systemErrorsImg").attr("src", "/wfe/images/view_setup_hidden.gif");
				systemErrorsVisible = false;
			} else {
				$("#systemErrorsContentDiv").show();
				$("#systemErrorsImg").attr("src", "/wfe/images/view_setup_visible.gif");
				systemErrorsVisible = true;
			}
		});
		$("#processErrorsButton").click(function() {
			if (processErrorsVisible) {
				$("#processErrorsContentDiv").hide();
				$("#processErrorsImg").attr("src", "/wfe/images/view_setup_hidden.gif");
				processErrorsVisible = false;
			} else {
				$("#processErrorsContentDiv").show();
				$("#processErrorsImg").attr("src", "/wfe/images/view_setup_visible.gif");
				processErrorsVisible = true;
			}
		});
		$("a[fileName]").each(function() {
			$(this).click(function() {
				editScript($(this).attr("fileName"), "<bean:message key="button.save" />", "<bean:message key="button.execute" />", "<bean:message key="button.cancel" />");
			});
		});
		$("div.processErrorsFilter input").change(function() {
			var typeLabel = $(this).closest("label").text();
			var tds = $("#processErrorsContentDiv table tr").find("td:eq(0)").filter(":contains(" + typeLabel + ")");
			if ($(this).prop("checked")) {
				tds.closest("tr").show();
			} else {
				tds.closest("tr").hide();
			}
		});
	});
	</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/xmleditor/codemirror.js?"+Version.getHash() %>' />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/scripteditor.js?"+Version.getHash() %>' />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/errorviewer.js?"+Version.getHash() %>' />">c=0;</script>
</tiles:put>

<tiles:put name="body" type="string" >
<%
	String substitutionCriteriaIds = "";
	if (request.getParameter("substitutionCriteriaIds") != null) {
	    substitutionCriteriaIds = request.getParameter("substitutionCriteriaIds");
	}
%>

<wf:updatePermissionsOnSystemForm>
	<table width="100%">
	<tr>
		<td align="left">
			<wf:grantLoginPermissionOnSystemLink  />
		</td>
		<td align="right">
			<wf:showSystemLogLink href='<%= "/show_system_logs.do" %>'/>
		</td>
	</tr>
	</table>
</wf:updatePermissionsOnSystemForm>

<wf:listSubstitutionCriteriasForm buttonAlignment="right" substitutionCriteriaIds="<%= substitutionCriteriaIds %>">
	<table width="100%">
	<tr>
		<td align="left">
			<wf:addSubstitutionCriteriaLink  />
		</td>
	</tr>
	</table>
</wf:listSubstitutionCriteriasForm>

<table class='box'><tr><th class='box'><bean:message key="adminkit.scripts" /></th></tr>
<tr><td class='box'>
	<div style="position: relative;">
		<div style="position: absolute; right: 5px; top: 5px;">
			<table><tbody><tr>
				<td class="hideableblock">
					<a id="storageButton" href="javascript:void(0)" class="link">
						<img id="storageImg" class="hideableblock" src="/wfe/images/view_setup_visible.gif">
						&nbsp;<bean:message key="adminkit.savedscripts" />
					</a>
				</td>
			</tr></tbody></table>
		</div>
		<div>
			<table>
				<tr>
					<td class='hideableblock'>
						<a href="javascript:void(0)" class='link' onclick='javascript:uploadScript("<bean:message key="button.save" />", "<bean:message key="button.execute" />", "<bean:message key="button.cancel" />");'><bean:message key="button.upload" /></a>
						&nbsp;&nbsp;&nbsp;
						<a href="javascript:void(0)" class='link' onclick='javascript:editScript("", "<bean:message key="button.save" />", "<bean:message key="button.execute" />", "<bean:message key="button.cancel" />");'><bean:message key="button.create" /></a>
					</td>
				</tr>
				<tr>
					<td>
					</td>
				</tr>
			</table>
		</div>
		<div id="storageContentDiv">
			<wf:viewAdminkitScripts />
		</div>
	</div>
</td></tr></table>

<wf:exportDataFile />

<wf:importDataFile />

<table class='box'><tr><th class='box'><bean:message key="title.monitoring" /></th></tr>
	<tr>
		<td class='box'>
			<table>
				<tr>
					<td>
						<a href="/wfe/monitoring" class="link" target="javamelody">JavaMelody</a>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>

<table class='box'><tr><th class='box'><bean:message key="title.errors" /></th></tr>
<tr><td class='box'>
	<div>
		<a id="systemErrorsButton" href="javascript:void(0)" class="link">
			<img id="systemErrorsImg" class="hideableblock" src="/wfe/images/view_setup_visible.gif">
			&nbsp;<bean:message key="errors.system" />
		</a>
	</div>
	<div id="systemErrorsContentDiv"> 
		<wf:viewSystemErrors />
	</div>
	<br />
	<div>
		<a id="processErrorsButton" href="javascript:void(0)" class="link">
			<img id="processErrorsImg" class="hideableblock" src="/wfe/images/view_setup_visible.gif">
			&nbsp;<bean:message key="errors.processes" />
		</a>
		<a href="/wfe/activateFailedProcesses.do" style="float: right;">
			<bean:message key="failed.processes.activate" />
		</a>
	</div>
	<div id="processErrorsContentDiv">
		<wf:viewProcessErrors />
	</div>
</td></tr>
<tr><td align='right' class='box'>
	<input value='<bean:message key="button.support" />' class='button' onclick="showSupportFiles();" type='button'>
</td></tr>
</table>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>