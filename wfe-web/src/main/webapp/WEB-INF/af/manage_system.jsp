<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string" />

<tiles:put name="body" type="string" >
<wf:managePermissionsForm securedObjectType="SYSTEM">
	<table width="100%">
	<tr>
		<td align="left">
			<wf:grantPermissionsLink securedObjectType="SYSTEM"/>
		</td>
		<td align="right">
			<wf:showSystemLogLink href='<%= "/show_system_logs.do" %>'/>
		</td>
	</tr>
	</table>
</wf:managePermissionsForm>

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
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>