let deleteMessageType = "deleteMessage";
let newMessageType = "newMessage";
let editMessageType = "editMessage";
let editMessageId = -1;

function deleteMessageHandler(id) {
    if (confirm("Вы уверены?")) {
        let newMessage = {};
        newMessage.id = id;
        newMessage.processId = $("#ChatForm").attr("processId");
        newMessage.messageType = deleteMessageType;
        sendBinaryMessage(chatSocket, newMessage);
    }
}

function sendMessageHandler() {
    let message = document.getElementById("message").value;
    if ((message !== "") || (attachedFiles.length !== 0)) {
        message = replaceLinks(message);
        let newMessage = {};
        newMessage.text = message;
        newMessage.processId = $("#ChatForm").attr("processId");
        if (editMessageId === -1) {
            newMessage.messageType = newMessageType;
            newMessage.isPrivate = $("#isPrivate").prop("checked");
            if (attachedFiles.length > 0) {
                bindFilesAndSendMessage(attachedFiles, newMessage)
            } else {
                sendBinaryMessage(chatSocket, newMessage);
            }
        } else if (confirm("Вы уверены?")) {
            newMessage.messageType = editMessageType;
            newMessage.id = editMessageId;
            editMessageId = -1;
            sendBinaryMessage(chatSocket, newMessage);
        }
        $("#message").val("");
    }
    return 0;
}

function replaceLinks(message) {
    message = message.replace(/(^|[^\/"'>\w])(http:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
    message = message.replace(/(^|[^\/"'>\w])(https:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
    message = message.replace(/(^|[^\/"'>\w])(www\.)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='http://$2$3$4'>$2$3$4</a>");
    return message;
}
