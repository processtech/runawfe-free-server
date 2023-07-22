<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <tiles:put name="head" type="string">
		<script type="text/javascript">
			var signalSentMessage = "<bean:message key="signal.message_is_sent" />";
		</script>
		<script type="text/javascript" src="<html:rewrite page='<%="/js/sendprocesssingal.js"%>' />">c=0;</script>
		<style>
			th.list {
				width: 50%;
			}
			td.list input {
				width: 90%;
			}
			td.list:nth-child(2) input {
				width: 300px;
			}
		</style>
	</tiles:put>
    <tiles:put name="body" type="string">
        <wf:sendProcessSignalForm />
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>