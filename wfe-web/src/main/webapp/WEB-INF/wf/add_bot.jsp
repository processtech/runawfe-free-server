<%@ page language="java" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <%@include file="/WEB-INF/af/chat_notifications.jsp" %>

    <tiles:put name="head" type="string">
        <script type="text/javascript">
            $(document).ready(function() {
                var sequential = $("input[name='sequential']");
                var transactionalTimeout = $("input[name='transactionalTimeout']");
                var checked = sequential.prop('checked');
                $("input[name='transactional']").change(function() {
                    if(this.checked) {
                        sequential.prop('checked', true).prop('disabled', true);
                        transactionalTimeout.prop('disabled', false);
                    } else {
                        sequential.prop('checked', checked).prop('disabled', false);
                        transactionalTimeout.prop('disabled', true);
                    }
                });
            });
        </script>
    </tiles:put>

	<tiles:put name="body" type="string">
        <%
            long id = Long.parseLong(request.getParameter("botStationId"));
        %>
        <wf:addBotTag botStationId="<%= id %>"/>
    </tiles:put>

    <tiles:put name="messages" value="../common/messages.jsp"/>

</tiles:insert>