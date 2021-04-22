<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="ru.runa.common.Version" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title></title>
<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/main.css?"+Version.getHash() %>' />">
<link rel="stylesheet" type="text/css" href="<html:rewrite page="/css/jquery-ui-1.9.2.custom.css" />">
<script type="text/javascript" src="<html:rewrite page="/js/jquery-1.8.3.min.js" />"></script>
<script type="text/javascript">
<% 
Long taskId = Long.valueOf(request.getParameter("taskId"));
String token = "Bearer " + request.getParameter("jwt");
%>
function getHtmlForm() {
    $.ajax({
         url: '<%=request.getContextPath()%>/getForm',
         type: 'POST',
         dataType: 'html',
         data: {
            taskId: <%=taskId%>
         },
         headers: {
             'Authorization':'<%=token%>'
         },
         crossDomain: true,
         success: function (data) {
              if (data) {
                   $('body').html(data);
              }
         },
         error: function (jqXHR, textStatus, errorThrown) {
              console.log("Error: " + textStatus + " Cause: " + errorThrown);
         }
    });
}
$(document).ready(function () {
    getHtmlForm();
});
</script>
</head>
<body>
</body>
</html>