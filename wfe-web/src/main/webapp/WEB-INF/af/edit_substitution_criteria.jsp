<%@page import="ru.runa.common.Version"%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	String idParameter = request.getParameter("id");
	Long id = null;
	if (idParameter != null && !"null".equals(idParameter)) {
		id = Long.parseLong(idParameter);
	}
%>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/substitutionCriteria.js?"+Version.getHash() %>' />"></script>
	<wf:updateSubstitutionCriteriaForm identifiableId="<%= id %>" />
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>