<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <script type="text/javascript" src="<html:rewrite page="/js/jquery-1.8.3.min.js" />">c = 0;</script>
</head>
<body>
<script type="text/javascript">
    $(document).ready(function () {
        initChatSocket(establishWebSocketConnection({
            "newMessage": newMessageAlerter,
            "errorMessage": errorMessageAlerter
        }));
    });
</script>
</body>
</html>
