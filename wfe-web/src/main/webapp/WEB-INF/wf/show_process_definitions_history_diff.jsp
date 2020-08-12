<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>

<%@ page import="ru.runa.common.WebResources" %>
<%@ page import="ru.runa.common.web.ProfileHttpSessionHelper" %>
<%@ page import="ru.runa.wfe.presentation.BatchPresentation" %>
<%@ page import="ru.runa.wfe.presentation.filter.StringFilterCriteria" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<style>
		.diffView {
			border: 1px solid #B6AD84;
			width: 100%;
			padding: 5px;
		}
		.diffView table td {
			white-space: nowrap;
			font-family: monospace;
		}
		.diffView table td pre {
			margin: 0px;
		}
		.diffView table td.added {
			color: darkcyan;
		}
		.diffView table td.deleted {
			color: brown;
		}
		.diffView table td.unchanged {
			color: gray;
		}
		.diffView table td.comment {
			color: lightgray;
		}
		.diffView table td.error {
			color: red;
		}
	</style>
</tiles:put>
<tiles:put name="body" type="string">

	<div class="diffView">
		<%= request.getAttribute("diffContent") %>
	</div>

</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>