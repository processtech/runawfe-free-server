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
<wf:exportDataFile />
<wf:importDataFile />
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />

</tiles:insert>