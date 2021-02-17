let newMessageType = "newMessage";
let editMessageType = "editMessage";
let deleteMessageType = "deleteMessage";
let readMessageType = "readMessage";

let editMessageId = -1;

function deleteMessageHandler(id) {
    if (confirm("Вы уверены?")) {
        let newMessage = {};
        newMessage.messageId = id;
        newMessage.processId = $("#ChatForm").attr("processId");
        newMessage.messageType = deleteMessageType;
        sendBinaryMessage(chatSocket, newMessage);
    }
}

function sendMessageHandler() {
    let message = document.getElementById("message").value;
    if ((message !== "") || (attachedFiles.length !== 0)) {
        message = message.replace(/(^|[^\/"'>\w])(http:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
        message = message.replace(/(^|[^\/"'>\w])(https:\/\/)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='$2$3$4'>$2$3$4</a>");
        message = message.replace(/(^|[^\/"'>\w])(www\.)(\S+)([\wа-яёЁ\/\-]+)/ig, "$1<a href='http://$2$3$4'>$2$3$4</a>");
        message = message.replace(/\r?\n/g, "<br />");
        let newMessage = {};
        newMessage.message = message;
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
            newMessage.editMessageId = editMessageId;
            editMessageId = -1;
            sendBinaryMessage(chatSocket, newMessage);
        }
        $("#message").val("");
    }
    return 0;
}
