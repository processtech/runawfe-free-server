<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="af" %>

<af:globalExceptions />

<center>

<%--Used for process errors displaying --%>
<%
	Object processErrors = request.getAttribute("processErrors");
	if (processErrors != null) {
		out.write("<div class=\"errors\" style=\"color: #914b98;\">");
		out.write("<b>"+ru.runa.common.web.Commons.getMessage("errors.process", pageContext)+"</b><br>");
		out.write("" + processErrors);
		out.write("</div>");
	}
%>

<%--Used for ajax errors displaying --%>
<div id="ajaxErrorsDiv" class="errors" style="font-weight: bold; color: #914b98;">
</div>

<%--Used for error message displaying --%>
<span class="errors">
	<html:messages id="commonError" property="org.apache.struts.action.GLOBAL_MESSAGE">
		<BR>
		<B style="color: red;"><bean:write name="commonError" /></B>
	</html:messages>
	<html:messages id="processError" property="processErrors">
		<BR>
		<B style="color: brown;"><bean:write name="processError" /></B>
	</html:messages>
	<html:messages id="userMessage" property="userMessages">
		<BR>
		<B style="color: blue;"><bean:write name="userMessage" /></B>
	</html:messages>
</span>
</center>