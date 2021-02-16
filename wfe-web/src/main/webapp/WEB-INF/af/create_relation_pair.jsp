<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="body" type="string" >
<%
	long relationId = Long.parseLong(request.getParameter("relationId"));
%>
	<wf:createRelationPairForm relationId="<%= relationId %>"  />
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>