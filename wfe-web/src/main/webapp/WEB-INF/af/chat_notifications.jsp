<%@ page contentType="text/html;charset=UTF-8" %>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
</head>
<body>
<script type="text/javascript">
    $(document).ready(function() {
        function onMessage(event) {
            let message = JSON.parse(event.data);
            console.info(message);
            if (message.messageType === "newMessage") {
                let numberNewMessagesTD = $("#numberNewMessages" + message.processId);
                numberNewMessagesTD.text(Number.parseInt(numberNewMessagesTD.text()) + 1);
                alert("New Message!");
            }
        }

        establishConnection(onMessage);
    });
</script>
</body>
</html>
