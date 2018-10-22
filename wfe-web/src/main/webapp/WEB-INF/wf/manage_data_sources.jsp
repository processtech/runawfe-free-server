<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

	<tiles:put name="head" type="string">
		<script type="text/javascript" src="<html:rewrite page='<%="/js/datasource.js"%>' />">c=0;</script>
		<script type="text/javascript">
			var savePasswordSuccessMessage = "<bean:message key="datasource.password.save.success" />";
		</script>
	</tiles:put>

    <tiles:put name="body" type="string">
	    <wf:deployDataSource />
        <wf:dataSourceList buttonAlignment="right">
        </wf:dataSourceList>
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>

</tiles:insert>
