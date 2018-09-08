<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string" />

<tiles:put name="body" type="string" >
<wf:managePermissionsForm securedObjectType="SUBSTITUTION_CRITERIAS">
	<table width="100%">
		<tr>
			<td align="left">
				<wf:grantPermissionsLink securedObjectType="SUBSTITUTION_CRITERIAS"/>
			</td>
		</tr>
	</table>
</wf:managePermissionsForm>


<%
	String substitutionCriteriaIds = "";
	if (request.getParameter("substitutionCriteriaIds") != null) {
	    substitutionCriteriaIds = request.getParameter("substitutionCriteriaIds");
	}
%>
<wf:listSubstitutionCriteriasForm buttonAlignment="right" substitutionCriteriaIds="<%= substitutionCriteriaIds %>">
	<table width="100%">
	<tr>
		<td align="left">
			<wf:addSubstitutionCriteriaLink  />
		</td>
	</tr>
	</table>
</wf:listSubstitutionCriteriasForm>
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>