<%@ page pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.Version"%>
<%@ page import="ru.runa.common.web.Commons"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script type="text/javascript" src="<html:rewrite page='<%="/js/processgraphutils.js?"+Version.getHash() %>' />">c=0;</script>
	<script type="text/javascript" src="/wfe/js/i18n/processupgrade.dialog-<%= Commons.getLocale(pageContext).getLanguage() %>.js">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/processupgrade.dialog.js?"+Version.getHash() %>' />">c=0;</script>
	<script>
		$(document).ready(function() {
			var container = $("td.subprocessBindingDate");
			container.find("a").css({ "padding-left": "5px", "padding-right": "5px" });
			container.find("a.change").click(function() {
				container.find(".editData, a.apply, a.cancel").show();
				container.find(".displayData, a.change").hide();
			});
			container.find("a.apply").click(function() {
				container.closest("form").submit();
			});
			container.find("a.cancel").click(function() {
				container.find(".editData, a.apply, a.cancel").hide();
				container.find(".displayData, a.change").show();
			});
		});
	</script>
</tiles:put>

<tiles:put name="body" type="string" >
<% long id = Long.parseLong(request.getParameter("id")); %>

<wf:processDefinitionInfoForm identifiableId='<%= id %>'>	
<table width="100%">
	<tr>
		<td align="right">
			<wf:managePermissionsLink securedObjectType="DEFINITION" identifiableId="<%= id %>" />
		</td>
	<tr>
</table>
</wf:processDefinitionInfoForm>

<wf:listProcessDefinitionChangesForm identifiableId='<%= id %>'>
</wf:listProcessDefinitionChangesForm>

<wf:redeployDefinitionForm identifiableId='<%= id %>'  />

<wf:definitionGraphForm identifiableId='<%= id %>' />	


</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>