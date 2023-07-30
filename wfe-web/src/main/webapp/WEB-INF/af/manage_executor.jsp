<%@ page pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.Commons" %>
<%@ page import="ru.runa.wfe.security.SecuredObjectType" %>
<%@ page import="ru.runa.wfe.service.delegate.Delegates" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string">
<%
	long id = Long.parseLong(request.getParameter("id"));
	String returnAction="/manage_executor.do?id=" + id;
	SecuredObjectType securedObjectType = Delegates.getExecutorService().getExecutor(Commons.getUser(pageContext.getSession()), id).getSecuredObjectType();
%>
<wf:updateExecutorDetailsForm identifiableId="<%= id %>">
    <table width="100%">
    <tr>
		<td align="right">
		</td>
<%
	if (id == Commons.getUser(pageContext.getSession()).getActor().getId()) {
%>
		<td width="200" align="right">
			<a href="javascript:void(0);" onclick="location.assign(location.protocol + '//' + location.host + '/spa');" class="link">Рабочий интерфейс</a>
		</td>
<%
    }
%>
		<td width="200" align="right">
			<wf:listExecutorTasksLink identifiableId='<%= id %>' href='<%= "/manage_observable_tasks.do?executorId=" + id %>' />
		</td>
		<td width="200" align="right">
			<wf:managePermissionsLink securedObjectType="<%= securedObjectType.getName() %>" identifiableId="<%= id %>" />
		</td>
	</tr>
</table>
</wf:updateExecutorDetailsForm>

<wf:updateDigitalSignatureDetailsForm identifiableId="<%= id %>">
    <table width="100%">
        <tr>
		    <td align="left">
			    <wf:createDigitalSignatureLink identifiableId='<%= id %>' href='<%= "/create_digital_signature.do?executorId=" + id %>' />
		    </td>
		</tr>
    </table>
</wf:updateDigitalSignatureDetailsForm>

<wf:updateStatusForm identifiableId="<%= id %>"  />

<wf:updatePasswordForm identifiableId="<%= id %>"  />

<wf:listExecutorGroupsForm batchPresentationId="listExecutorGroupsForm" buttonAlignment="right" identifiableId="<%= id %>" returnAction="<%= returnAction %>" >
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listExecutorGroupsForm"  returnAction="<%= returnAction %>" >
			<wf:tableViewSetupForm batchPresentationId="listExecutorGroupsForm" returnAction="<%= returnAction %>" />
		</wf:viewControlsHideableBlock>
	</div>
	<wf:addExecutorToGroupsLink identifiableId='<%= id %>' href='<%= "/add_executor_to_groups.do?id=" + id %>' />
</wf:listExecutorGroupsForm>

<wf:listGroupMembersForm batchPresentationId="listGroupMembersForm" buttonAlignment="right" identifiableId="<%= id %>" returnAction="<%= returnAction %>">
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listGroupMembersForm"  returnAction="<%= returnAction %>"  >

			<wf:tableViewSetupForm batchPresentationId="listGroupMembersForm"  returnAction="<%= returnAction %>"/>

		</wf:viewControlsHideableBlock>
	</div>
	<wf:addGroupMembersLink identifiableId='<%=id %>' href='<%= "/add_members_to_group.do?id=" + id %>'  />
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
