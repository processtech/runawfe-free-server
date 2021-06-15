<%@ page import="ru.runa.common.Version" %>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="<html:rewrite page='<%="/css/chat_page.css?"+Version.getHash() %>' />">
    <script type="text/javascript" src="<html:rewrite page="/js/jquery-1.8.3.min.js" />">c = 0;</script>
    <script type="text/javascript" src="<html:rewrite page='<%="/js/chatHandlers.js" %>' />">c = 0;</script>
    <script type="text/javascript" src="<html:rewrite page='<%="/js/chatFiles.js" %>' />">c = 0;</script>
</head>
<body>
<script type="text/javascript">
    function sendMessage() {
        if (document.getElementById("message").value === "" && attachedFiles.length === 0) {
            sendChatForm();
        } else {
            sendMessageHandler();
        }
    }

    function editMessage(id, text) {
        $("#message").val(document.getElementById("message").value + text);
        editMessageId = id;
        openChatForm();
    }

    function reply(text) {
        $("#message").val(document.getElementById("message").value + " > " + text + "\n");
        openChatForm();
    }

    function deleteMessage(id) {
        deleteMessageHandler(id);
    }

    function openChatForm() {
        $("#ChatForm").css("display", "block");
        $(document.getElementsByName("submitButton")).css("display", "none");
    }

    function sendChatForm() {
        if ($("#ChatForm").css('display') !== 'none' && $("#variableSelect").val() !== undefined) {
            $(document.getElementsByName("submitButton")).click();
        }
    }
</script>
</body>
</html>
