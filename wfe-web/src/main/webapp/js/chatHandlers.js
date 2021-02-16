let newMessageType = "newMessage";
let editMessageType = "editMessage";
let deleteMessageType = "deleteMessage";
let readMessageType = "readMessage";

let editMessageFlag = false;
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
        if (editMessageFlag === false) {
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
            sendBinaryMessage(chatSocket, newMessage);
            editMessageFlag = false;
            editMessageId = -1;
        }
        $("#message").val("");
    }
    return 0;
}

function bindFilesAndSendMessage(files, message) {
    let fileToBase64Promises = [];
    for (let i = 0; i < files.length; i++) {
        fileToBase64Promises.push(fileToBase64(files[i]));
    }
    Promise.all(fileToBase64Promises).then(function () {
        message.files = attachedFilesBase64;
        sendBinaryMessage(chatSocket, message)
        attachedFilesBase64 = {};
        attachedFiles = [];
        $("#progressBar").css({"display": "none"});
        $("#filesTable").empty();
    }).catch(function (error) {
        alert(error.message);
    });
}
