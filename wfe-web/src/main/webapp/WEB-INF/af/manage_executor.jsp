<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	long id = Long.parseLong(request.getParameter(parameterName));
	String returnAction="/manage_executor.do?" + parameterName+ "=" +id;
%>

<wf:updateExecutorDetailsForm identifiableId="<%= id %>">
<table width="100%">
	<tr>
		<td align="right">
			<wf:updatePermissionsOnIdentifiableLink identifiableId='<%=id %>' href='<%= "/manage_executor_permissions.do?" + parameterName + "=" + id %>'  />
		</td>
	</tr>
	<tr>
		<td align="right">
			<wf:listExecutorTasksLink identifiableId='<%= id %>' href='<%= "/manage_all_tasks.do?" + parameterName + "=" + id %>'  />
		</td>
	</tr>
</table>
</wf:updateExecutorDetailsForm>

<wf:updateStatusForm identifiableId="<%= id %>"  />

<wf:updatePasswordForm identifiableId="<%= id %>"  />

<wf:listExecutorGroupsForm batchPresentationId="listExecutorGroupsForm" buttonAlignment="right" identifiableId="<%= id %>" returnAction="<%= returnAction %>" >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listExecutorGroupsForm"  returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listExecutorGroupsForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
	<wf:addExecutorToGroupsLink identifiableId='<%=id %>' href='<%= "/add_executor_to_groups.do?" + parameterName + "=" + id %>' />	
</wf:listExecutorGroupsForm>


<wf:listGroupMembersForm batchPresentationId="listGroupMembersForm" buttonAlignment="right" identifiableId="<%= id %>" returnAction="<%= returnAction %>" >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listGroupMembersForm"  returnAction="<%= returnAction %>"  >
			<wf:tableViewSetupForm batchPresentationId="listGroupMembersForm"  returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
	<wf:addGroupMembersLink identifiableId='<%=id %>' href='<%= "/add_members_to_group.do?" + parameterName + "=" + id %>'  />
</wf:listGroupMembersForm>

<wf:listExecutorRightRelationsForm identifiableId="<%= id %>" >
</wf:listExecutorRightRelationsForm>

<wf:listExecutorLeftRelationsForm identifiableId="<%= id %>" >
</wf:listExecutorLeftRelationsForm>

<wf:listSubstitutionsForm identifiableId="<%= id %>" buttonAlignment="right">
	<wf:addSubstitutionLink identifiableId='<%= id %>' text="button.add_substitution" href='<%= "/createSubstitution.do?actorId=" + id %>' />	
	<wf:addSubstitutionLink identifiableId='<%= id %>' text="button.add_terminator" href='<%= "/createSubstitution.do?actorId=" + id + "&terminator=true" %>' />	
</wf:listSubstitutionsForm>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>
