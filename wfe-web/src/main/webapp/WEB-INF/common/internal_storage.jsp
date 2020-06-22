<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<%
	String workbookPath = (String) request.getAttribute("workbookPath");
	String sheetContent = (String) request.getAttribute("sheetContent");
%>

<tiles:put name="head" type="string">
	<style>
		.internalStorageView {
			border: 1px solid #B6AD84;
			width: 100%;
			min-height: 400px;
		}
		table.internalStorage td {
			font-family: Lucida console;
			font-size: 11px;
		}
		table.internalStorage td.lineNumbers {
			color: red;
			text-align: right;
			padding-right: 5px;
			border-right: 1px solid #B6AD84;
		}
		table.internalStorage td.content {
			white-space: nowrap;
		}
	</style>
 </tiles:put>

<tiles:put name="body" type="string" >
	<b><%= workbookPath %></b><br />
	<wf:viewSheets workbookPath="<%= workbookPath %>" />
	<% if (sheetContent != null) { %>
		<html:form styleId="downloadForm" action="/viewInternalStorage" method="get">
			<html:hidden property="workbookPath" />
			<input type="hidden" name="mode" value="5" />
			<table class="box"><tr><th class="box">
				<a href="javascript:$('#downloadForm').submit();" style="color: white;"><bean:write name="viewInternalStorageForm" property="sheetName" />
			</th></tr></table>
		</html:form>
		<%= sheetContent %>
	<% } %>
 </tiles:put>
 
<tiles:put name="messages" value="../common/messages.jsp" />

</tiles:insert>
