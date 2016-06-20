<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ page import="ru.runa.common.web.Commons" %>
<%@ page import="ru.runa.wfe.commons.CalendarUtil" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

    <tiles:put name="body" type="string">
        <%
            String returnAction = "/manage_suspended_processes.do";
        %>
        
        <bean:message key="tab.processes.suspended"/>
        
        
        <c:choose>
            <c:when test="${not empty processListEmpty}">
                      <bean:message key="label.none"/>
            </c:when>
            <c:otherwise>
                <table border="0" cellpadding="4" cellspacing="0" >
                    <tr>
                        <th width="5%" >
                            <bean:message key="batch_presentation.process.id"/>
                        </th>
                       
                        <th width="5%">
                            <bean:message key="batch_presentation.process.started"/>
                        </th>
                        <th width="90%">
                            <bean:message key="batch_presentation.process.definition_name"/>
                        </th>

                        <th width="5%" ></th>
                    </tr>

                    <c:forEach var="process" items="${processList}">
                        <tr>
                            <td><c:out value="${process.id}"/></td>
                            
                            <td>
                                <%=CalendarUtil.formatDateTime(((ru.runa.wfe.execution.Process) pageContext.findAttribute("process")).getStartDate())%></td>
                            <td>
                                <c:out value="${process.deployment.name}"/>
                            </td>
                            <td>
                                <c:url var="restartProcessUrl" value="/restartSuspendedProcess.do">
                                    <c:param name="id" value="${process.id}"/>
                                </c:url>
                                
                                <a href="<c:out value='${restartProcessUrl}'/>" onclick="return confirm('<%= Commons.getMessage("confirmpopup.start.process", pageContext)%>')" >
                                    <%= Commons.getMessage("label.start_process", pageContext)%>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </c:otherwise>
        </c:choose>

    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>