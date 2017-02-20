<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
<script type="text/javascript">
var group = "<bean:message key="batch_presentation.executor.group" />";
var user = "<bean:message key="batch_presentation.executor.user" />";
var temporary_group = "<bean:message key="batch_presentation.executor.temporary_group" />";
$(document).ready(function() {
	var input = $("table.view-setup tr[field='batch_presentation.executor.actortype'] input[name='fieldsToFilterCriterias']");
	var select = $("<select />", { name: input.attr("name") });
	$("<option />", {val: "", text: ""}).appendTo(select);
	$("<option />", {val: "Y", text: group}).appendTo(select);
	$("<option />", {val: "N", text: user}).appendTo(select);
	$("<option />", {val: "D", text: temporary_group}).appendTo(select);
	select.val(input.val());
	input.replaceWith(select);
});
</script>
</tiles:put>
<tiles:put name="body" type="string">
<%
	String returnAction = "/manage_executors.do";
%>
<wf:listAllExecutorsForm  batchPresentationId="listAllExecutorsForm" buttonAlignment="right"  returnAction="<%= returnAction %>" >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listAllExecutorsForm"  returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listAllExecutorsForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
	<wf:createActorLink />
	&nbsp;&nbsp;&nbsp;
	<wf:createGroupLink />
<%
if (ru.runa.common.WebResources.isLDAPSynchronizationEnabled()) {
%>
	&nbsp;&nbsp;&nbsp;
	<wf:synchronizeExecutorsLink />
<%
}
%>
</wf:listAllExecutorsForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>