<%@page import="ru.runa.common.Version" %>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/chat.css?"+Version.getHash() %>' />">
    <script type="text/javascript" src="<html:rewrite page="/js/jquery-1.8.3.min.js" />">c = 0;</script>
    <script type="text/javascript" src="<html:rewrite page='<%="/js/chatHandlers.js" %>' />">c = 0;</script>
</head>
<body>
<script type="text/javascript">
    function sendMessage() {
        sendMessageHandler();
    }

    function editMessage(id, text) {
        $("#message").append(text);
        editMessageId = id;
    }

    function reply(text) {
        $("#message").append(" > " + text + "\n");
    }

    function deleteMessage(id) {
        deleteMessageHandler(id);
    }
</script>
</body>
</html>
