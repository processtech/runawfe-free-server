<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
	<%@include file="/WEB-INF/af/chat_notifications.jsp" %>

<tiles:put name="head" type="string">
<script type="text/javascript">
var groupLabel = "<bean:message key="batch_presentation.executor.group" />";
var userLabel = "<bean:message key="batch_presentation.executor.user" />";
var temporaryGroupLabel = "<bean:message key="batch_presentation.executor.temporary_group" />";
var escalationGroupLabel = "<bean:message key="batch_presentation.executor.escalation_group" />";
var delegationGroupLabel = "<bean:message key="batch_presentation.executor.delegation_group" />";
$(document).ready(function() {
	var input = $("table.view-setup tr[field='batch_presentation.executor.type'] input[name='fieldsToFilterCriterias']");
	var select = $("<select />", { name: input.attr("name") });
	$("<option />", {val: "", text: ""}).appendTo(select);
	$("<option />", {val: "Y", text: groupLabel}).appendTo(select);
	$("<option />", {val: "N", text: userLabel}).appendTo(select);
	$("<option />", {val: "T", text: temporaryGroupLabel}).appendTo(select);
	$("<option />", {val: "E", text: escalationGroupLabel}).appendTo(select);
	$("<option />", {val: "D", text: delegationGroupLabel}).appendTo(select);
	select.val(input.val());
	input.replaceWith(select);
	$("table.view-setup tr[field='batch_presentation.executor.type'] img[class='button-more']").remove();
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
if (ru.runa.wfe.security.logic.LdapProperties.isSynchronizationEnabled()) {
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