<%@page import="ru.runa.wfe.commons.GitProperties"%>
<%@ page language="java" pageEncoding="UTF-8" session="false" %>
<%@ page import="java.net.URLDecoder" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>

<% 
	String userName = request.getParameter("login") == null ? "" : URLDecoder.decode(request.getParameter("login"), "utf-8");
	String userPwd = request.getParameter("password") == null ? "" : URLDecoder.decode(request.getParameter("password"), "utf-8");
%>

<html:html lang="true">
  <head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/main.css?"+GitProperties.getCommit() %>' />">
  </head>
	<body>
	<center>
			<table height = "100%">
				<tr height = "5%">
					<td></td>
				</tr>
				<tr height = "65%">
					<td align="center">
					   <html:form action="/login">
						<table>
							<tr>
								<td  align="left" colspan="2" target="new">
									<a href="http://runawfe.org">
										<img  border="0" src="<html:rewrite page="/images/big_logo.png"/>" alt="Runa WFE">
									</a>
								</td>
							</tr>
							<tr>			
								<td><bean:message key="login.page.login.message"/></td>
								<td style="width:170px"><input type="text" name="login" value="<%= userName %>" style="width: 100%;" class="required"></td>
				  			</tr>
							<tr>
		  						<td><bean:message key="login.page.password.message"/></td>
   	      						<td style="width:170px"><input type="password" name="password" value="<%= userPwd %>" style="width: 100%;"></td>
			  				</tr>
							<tr>
								<td>
									<html:submit>
										<bean:message key="login.page.login.button"/>
									</html:submit>
								</td>
							</tr>					
						</table>
						</html:form>
						<% if (ru.runa.common.WebResources.isNTLMSupported()) { %>
						<table>
							<tr>
								<td>
									<html:link action="/ntlmlogin">
										<bean:message key="login.page.login.ntlm"/>
									</html:link> 	
								</td>
							</tr>
						</table>
						<% } %>
						<% if (ru.runa.wfe.security.auth.KerberosLoginModuleResources.isHttpAuthEnabled()) { %>
						<table>
							<tr>
								<td>
									<html:link action="/krblogin">
										<bean:message key="login.page.login.kerberos"/>
									</html:link> 	
								</td>
							</tr>
						</table>
						<% } %>
						<jsp:include page="../common/messages.jsp" />
					</td>
				</tr>
				<tr>
					<td></td>
				</tr>
			</table>
		</center>
	</body>
</html:html>
