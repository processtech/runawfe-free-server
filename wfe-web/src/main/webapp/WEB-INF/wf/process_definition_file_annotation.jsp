<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
<style>
.tagPreForTextLines {
	margin-top: 0px;
	margin-bottom: 0px;
}

.tagDivForSpaceBetweenTextLines {
	height: 10px;
}

.haveTooltip .tooltip {
	visibility: hidden;
	position: absolute;
	font-weight: normal; /*иначе будет bold, как у родительского элемента*/
	color: black; /*иначе будет темно-красное, как цвет родительского элемента при наведении*/
	background-color: white; /*иначе будет прозрачный*/
	padding: 5px 5px;
	border: 2px solid;
	border-radius: 5px;
}

.haveTooltip:hover .tooltip {
	visibility: visible;
}
</style>
</tiles:put>

<tiles:put name="body" type="string">
	<wf:processDefinitionFileAnnotationForm />

	<wf:processDefinitionFileAnnotationChanges />
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>