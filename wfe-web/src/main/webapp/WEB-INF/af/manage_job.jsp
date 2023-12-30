<%@ page pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.Commons" %>
<%@ page import="ru.runa.wfe.security.SecuredObjectType" %>
<%@ page import="ru.runa.wfe.service.delegate.Delegates" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<% Long jobId = Long.parseLong(request.getParameter("jobId")); %>
<% Long processId = Long.parseLong(request.getParameter("processId")); %>
<tiles:put name="body" type="string">
    <wf:processInfoForm identifiableId="<%= processId %>" readOnly="true">
        <table width="100%">
            <tr>
                <td align="right">
                    <wf:showProcessLink identifiableId='<%= processId %>' />
                </td>
            </tr>
        </table>
    </wf:processInfoForm>
    <wf:manageJobForm jobId='<%= jobId %>' processId='<%= processId %>'/>
</tiles:put>

<tiles:put name="messages" value="../common/messages.jsp" />

</tiles:insert>
