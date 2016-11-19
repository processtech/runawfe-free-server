<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<%
	String parameterName = IdForm.ID_INPUT_NAME;
	long id = Long.parseLong(request.getParameter(parameterName));
	String subprocessId = request.getParameter("subprocessId");
%>

<wf:showGraphHistoryForm identifiableId="<%= id %>" subprocessId='<%= subprocessId %>'>	 
</wf:showGraphHistoryForm>