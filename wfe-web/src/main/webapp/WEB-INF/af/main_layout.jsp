<%@page import="org.springframework.mobile.device.LiteDeviceResolver"%>
<%@page import="org.springframework.mobile.device.DeviceResolver"%>
<%@page import="org.springframework.mobile.device.DeviceUtils"%>
<%@page import="org.springframework.mobile.device.Device"%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="af" %>
<%@ page import="ru.runa.common.web.Commons" %>
<%@ page import="ru.runa.common.WebResources" %>
<%@ page import="ru.runa.common.Version" %>
<!DOCTYPE HTML>
<%!
	private final static DeviceResolver DEVICE_RESOLVER = new LiteDeviceResolver();
%>
<% 
	Device currentDevice = DEVICE_RESOLVER.resolveDevice(request);
	boolean desktop = (currentDevice == null || !currentDevice.isMobile() && !currentDevice.isTablet());
	String thinInterface = (String)request.getAttribute("runawfe.thin.interface");
	String thinInterfacePage = (String)request.getAttribute("runawfe.thin.interface.page");
	if (thinInterfacePage == null) {
		thinInterfacePage="/start.do";
	}
%>
<html:html lang="true">
  <head>
  	<meta http-equiv="X-UA-Compatible" content="IE=Edge">
  	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
  	<meta http-equiv="Cache-Control" content="no-cache">
  	<meta http-equiv="Pragma" content="no-cache">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/main.css?"+Version.getHash() %>' />">
	<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/jquery-ui-1.9.2.custom.css" />">
	<script type="text/javascript">
		var saveSuccessMessage = "<bean:message key="adminkit.script.save.success" />";
		var executionSuccessMessage = "<bean:message key="adminkit.script.execution.success" />";
		var executionFailedMessage = "<bean:message key="adminkit.script.execution.failed" />";
		var buttonCloseMessage = "<bean:message key="button.close" />";
		var buttonSupportMessage = "<bean:message key="button.support" />";
		var loadingMessage = "<bean:message key="message.loading" />";
		var buttonCancelMessage = "<bean:message key="button.cancel" />";
		var currentBrowserLanguage = "<%= Commons.getLocale(pageContext).getLanguage() %>";
	</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery-1.8.3.min.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.cookie.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery-ui-1.9.2.custom.min.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.ui.mask.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/jquery.ui.timepicker.js" />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page="/js/safelog.js" />">c=0;</script>
<% if (!"en".equals(Commons.getLocale(pageContext).getLanguage())) { %>
	<script type="text/javascript" src="/wfe/js/i18n/jquery.ui.datepicker-<%= Commons.getLocale(pageContext).getLanguage() %>.js">c=0;</script>
	<script type="text/javascript" src="/wfe/js/i18n/jquery.ui.timepicker-<%= Commons.getLocale(pageContext).getLanguage() %>.js">c=0;</script>
<% } %>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/jquery.edit-list.js?"+Version.getHash() %>' />">c=0;</script>
	<script type="text/javascript" src="<html:rewrite page='<%="/js/common.js?"+Version.getHash() %>' />">c=0;</script>
	<script type="text/javascript">
		$(document).ready(function() {
		  $(document).ajaxError(function(event, request, settings, exception) {
		    $("#ajaxErrorsDiv").html("<bean:message key="ajax.request.error" />");
		  });
		  $(document).ajaxSend(function(event, request, settings) {
		    $("#ajaxErrorsDiv").html("<img src=\"/wfe/images/loading.gif\" align=\"absmiddle\">&nbsp;&nbsp;&nbsp;<bean:message key="ajax.request.inprogress" />");
		  });
		  $(document).ajaxSuccess(function(event, request, settings) {
		    $("#ajaxErrorsDiv").html("");
		  });
		});
	</script>
	<tiles:insert attribute="head" ignore="true"/>
  </head>
<body>
<% if (thinInterface == null || !thinInterface.equals("true")) { %>
	<table class="box">
		<tr>
			<td width="35%">
				<a href="http://runawfe.org" target="new">
					<img hspace="10" border="0" src="<html:rewrite page="/images/big_logo.gif"/>" alt="Runa WFE">
				</a>
				<script type="text/javascript">
					function refreshSystemMenu() {
						var systemMenu = $("#systemMenu");
						if (systemMenu.css("display") === "none") {
							systemMenu.css("display", "block")
							systemMenu.parent().css("width", "15%");
							systemMenu.parent().siblings().css("width", "85%");
						} else {
							systemMenu.css("display", "none");
							systemMenu.parent().css("width", "0%");
							systemMenu.parent().siblings().css("width", "100%");
						}
					}
				</script>
				<img hspace="0" border="0" src="<html:rewrite page="/images/menu.png"/>" onclick="refreshSystemMenu();">
			</td>
			<td width="65%" >
				<table width="100%">	
				<tr> 
					<td align="left" >
						<tiles:insert attribute="messages"/>
					</td>
					<td align="right">
						 <af:loginAsMessage message="<%= Commons.getMessage(\"label.logged_as\", pageContext) %>" /><br><af:logout />
					<td>
				</tr>
				</table>
			</td>
		</tr>
	</table>
	<table class="box">
		<tr>
			<td valign="top" height="100%" width='<%= desktop ? 15 : 0 %>%'>
				<div id="systemMenu" style='display: <%= desktop ? "block" : "none" %>;'>
				<hr>
				<table class="box">	
					<tr>
						<th class='box'><bean:message key="title.menu"/></th>
					</tr>
				</table>
				<af:tabHeader />
				<hr>
				<%-- //uncoment following lines for WFDEMO 
				<a href="http://sourceforge.net" target="new"><img src="http://sourceforge.net/sflogo.php?group_id=125156&amp;type=1" border="0" alt="SourceForge.net Logo"/></a>
				<BR>
				<B>Feedback</B> 
				<A HREF="http://sourceforge.net/forum/?group_id=125156" target="new">forum</A>
				--%>
				<%= WebResources.getAdditionalLinks() %>
				<div style="padding: 3px; color: #aaa;">
					<bean:message key="title.version"/> <b><%= Version.get() %></b>
				</div>
				<div style="padding: 3px; color: #ccc;">
					<bean:message key="title.build"/>
					<%= Version.getBuildInfo() %>
				</div>
				<div id="filtersHelpDialog" style="display: none;">
					<bean:message key="content.filters.help"/>
				</div>
				</div>
			</td>
			<td valign="top" height="100%" width="<%= desktop ? 85 : 100 %>%">
				<hr>
					<tiles:insert attribute="body"/>
				<hr>
			</td>
		</tr>
	</table>
<% } else { %>
	<a href='<html:rewrite action="<%=thinInterfacePage%>"/>' class='link'>Show start page</a>
	<tiles:insert attribute="messages"/>
	<tiles:insert attribute="body"/>
<% } %>
</body>
</html:html>
