<%@ page pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.Version"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script>
	var storageVisible = true;
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
		$("a[fileName]").each(function() {
			$(this).click(function() {
				editScript($(this).attr("fileName"), "<bean:message key="button.save" />", "<bean:message key="button.execute" />", "<bean:message key="button.cancel" />");
			});
		});
	});
	</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/xmleditor/codemirror.js?"+Version.getHash() %>' />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/scripteditor.js?"+Version.getHash() %>' />">c=0;</script>
</tiles:put>

<tiles:put name="body" type="string" >
<wf:managePermissionsForm securedObjectType="SCRIPTS">
	<table width="100%">
		<tr>
			<td align="left">
				<wf:grantPermissionsLink securedObjectType="SCRIPTS"/>
			</td>
		</tr>
	</table>
</wf:managePermissionsForm>

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
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />

</tiles:insert>