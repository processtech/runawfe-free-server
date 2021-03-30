<%@ page pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.Version"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script>
	var systemErrorsVisible = true;
	var processErrorsVisible = true;
	$(document).ready(function() {
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
	<script type="text/javascript" src="<html:rewrite page='<%="/js/errorviewer.js?"+Version.getHash() %>' />">c=0;</script>
</tiles:put>

<tiles:put name="body" type="string">
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
	<input value='<bean:message key="button.support" />' class='button' onclick="showSupportFiles();" type='button' <%= request.getAttribute("errorsExist") == null ? "disabled='disabled'" : "" %>>
</td></tr>
</table>
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>