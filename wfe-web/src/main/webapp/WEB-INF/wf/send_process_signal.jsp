<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <%@include file="/WEB-INF/af/chat_notifications.jsp" %>
    <tiles:put name="head" type="string">
		<script type="text/javascript" src="<html:rewrite page='<%="/js/sendprocesssingal.js"%>' />">c=0;</script>
	</tiles:put>
    <tiles:put name="body" type="string">
        <wf:sendProcessSignalForm />
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>