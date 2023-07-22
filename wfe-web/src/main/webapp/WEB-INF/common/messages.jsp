<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="af" %>

<af:globalExceptions />

<div style="float: right;">
<center>

<%--Used for process errors displaying --%>
<%
	Object processErrors = request.getAttribute("processErrors");
	if (processErrors != null) {
		out.write("<div class=\"errors\" style=\"color: #914b98;\">");
		out.write("<b>" + ru.runa.common.web.Commons.getMessage("errors.process", pageContext) + "</b><br>");
		out.write("" + processErrors);
		out.write("</div>");
	}
%>

<%--Used for ajax errors displaying --%>
<div id="ajaxErrorsDiv" class="errors" style="font-weight: bold; color: #914b98;">
</div>

<%--Used for error message displaying --%>
<span class="errors">
	<html:messages id="error">
		<BR>
		<B style="color: red;"><bean:write name="error" /></B>
	</html:messages>
	<html:messages id="message" message="true">
		<BR>
		<B style="color: blue;"><bean:write name="message" /></B>
	</html:messages>
</span>

<%--Used for chat alerts displaying --%>
	<div class="chatAlert">
		<b id="chatErrorAlert" style="color: red;"></b>
		<b id="chatNotificationAlert" style="color: blue;"></b>
	</div>
</center>
</div>