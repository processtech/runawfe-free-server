<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="ru.runa.common.Version" %>
<%@ page import="ru.runa.common.web.Commons" %>
<%@ page import="ru.runa.common.WebResources" %>
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
<script type="text/javascript" src="<html:rewrite page="/js/jquery.cookie.js" />">c=0;</script>
<script type="text/javascript" src="<html:rewrite page="/js/jquery-ui-1.9.2.custom.min.js" />">c=0;</script>
<script type="text/javascript" src="<html:rewrite page="/js/jquery.ui.mask.js" />">c=0;</script>
<script type="text/javascript" src="<html:rewrite page="/js/jquery.ui.timepicker.js" />">c=0;</script>
<% if (!"en".equals(Commons.getLocale(pageContext).getLanguage())) { %>
    <script type="text/javascript" src="/wfe/js/i18n/jquery.ui.datepicker-<%= Commons.getLocale(pageContext).getLanguage() %>.js">c=0;</script>
    <script type="text/javascript" src="/wfe/js/i18n/jquery.ui.timepicker-<%= Commons.getLocale(pageContext).getLanguage() %>.js">c=0;</script>
<% } %>
<script type="text/javascript" src="<html:rewrite page='<%="/js/jquery.edit-list.js?"+Version.getHash() %>' />">c=0;</script>
<script type="text/javascript" src="<html:rewrite page='<%="/js/common.v2.js?"+Version.getHash() %>' />">c=0;</script>
<script type="text/javascript" src="<html:rewrite page='<%="/js/taskformutils.js?"+Version.getHash() %>' />"></script>
<script type="text/javascript" src="/wfe/js/i18n/delegate.dialog-<%= Commons.getLocale(pageContext).getLanguage() %>.js?<%=Version.getHash()%>">c=0;</script>
<script type="text/javascript" src="<html:rewrite page='<%="/js/delegate.dialog.js?"+Version.getHash() %>' />">c=0;</script>
<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/fileupload.css?"+Version.getHash() %>' />">
<link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/delegate.dialog.css?"+Version.getHash() %>' />">
<% 
    for (String url : WebResources.getTaskFormExternalJsLibs()) {
%>
    <script type="text/javascript" src="<%= url %>"></script>
<% 
    }
%>
<script type="text/javascript">
<% 
Long taskId = Long.valueOf(request.getParameter("taskId"));
String token = "Bearer " + request.getParameter("jwt");
%>
var submitButton = null;
function getHtmlForm() {
    $.ajax({
         url: '<%=request.getContextPath()%>/getOldForm',
         type: 'POST',
         dataType: 'html',
         data: {
            taskId: <%=taskId%>
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
    $.ajaxSetup({
        headers: {
            'Authorization':'<%=token%>'
        }
    });
    getHtmlForm();
    $('body').on('submit', 'form#processForm', function() {
        var formElement = document.getElementById('processForm');
        var formData = new FormData(formElement);
        if (null === submitButton) {
            submitButton = $('form#processForm').find('input[name=submitButton]')[0];
        }
        formData.append(submitButton.name, submitButton.value);
        $.ajax({
            url: '<%=request.getContextPath()%>/submitOldTaskForm',
            type: 'POST',
            dataType: 'json',
            data: formData,
            crossDomain: true,
            processData: false,
            contentType: false,
            success: function (data) {
                 if (data) {
                      $('body').html('<b style="color: blue;">' + data.msg + '</b>');
                 }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                 console.log("Error: " + textStatus + " Cause: " + errorThrown);
            }
        });
        //for prevent default and stop bubbling up
        return false;
    });
    $('body').on('click', 'input[name=submitButton]', function() {
        submitButton = this;
    });
});
</script>
</head>
<body>
</body>
</html>