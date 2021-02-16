<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<%
    String securedObjectType = pageContext.getRequest().getParameter("type");
    String s = pageContext.getRequest().getParameter("id");
    Long identifiableId = s == null ? null : Long.parseLong(s);
%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <tiles:put name="body" type="string">
        <wf:managePermissionsForm securedObjectType="<%= securedObjectType %>" identifiableId="<%= identifiableId %>">
            <table width="100%">
                <tr>
                    <td align="left">
                        <wf:grantPermissionsLink securedObjectType="<%= securedObjectType %>" identifiableId="<%= identifiableId %>"/>
                        &nbsp;
                        <wf:returnLinkTag/>
                    </td>
                </tr>
            </table>
        </wf:managePermissionsForm>
    </tiles:put>

    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>