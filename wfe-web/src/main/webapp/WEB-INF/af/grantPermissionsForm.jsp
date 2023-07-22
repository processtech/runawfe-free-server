<%@ page import="ru.runa.common.web.Commons" %>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<%
    String securedObjectType = pageContext.getRequest().getParameter("type");
    String s = pageContext.getRequest().getParameter("id");
    Long identifiableId = s == null ? null : Long.parseLong(s);
    String returnAction = pageContext.getRequest().getParameter("return");

    // For batchPresentation chooser/editor, return action is current page URI.
    String selfAction = Commons.getSelfActionWithQueryString(pageContext);
%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <tiles:put name="body" type="string">
        <wf:grantPermissionsForm securedObjectType="<%= securedObjectType %>" identifiableId="<%= identifiableId %>"
                batchPresentationId="grantPermissionsForm" returnAction="<%= returnAction %>">
            <div>
                <wf:viewControlsHideableBlock hideableBlockId="grantPermissionsForm" returnAction="<%= selfAction %>">
                    <wf:tableViewSetupForm batchPresentationId="grantPermissionsForm" returnAction="<%= selfAction %>"/>
                </wf:viewControlsHideableBlock>
            </div>
        </wf:grantPermissionsForm>
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>