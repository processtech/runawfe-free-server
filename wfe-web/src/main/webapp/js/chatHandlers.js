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
    if ((message !== "") || (attachedPosts.length !== 0) || (attachedFiles.length !== 0)) {
        //ищем ссылки
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
            let idHierarchyMessage = "";
            for (let i = 0; i < attachedPosts.length; i++) {
                idHierarchyMessage += attachedPosts[i] + ":";
            }
            newMessage.idHierarchyMessage = idHierarchyMessage;
            if (attachedFiles.length > 0) {
                sendNewMessageAndBindFiles(attachedFiles, newMessage)
            } else {
                sendNewMessage(newMessage);
            }
        } else {
            newMessage.messageType = editMessageType;
            newMessage.editMessageId = editMessageId;
            sendBinaryMessage(chatSocket, newMessage);
            $("#message").val("");
            editMessageFlag = false;
            editMessageId = -1;
        }
        console.info(newMessage);
    }
    return 0;
}

function sendNewMessage(message) {
    sendBinaryMessage(chatSocket, message)
    $("#message").val("");
    let addReplays = document.getElementsByClassName("addReply");
    for (let i = 0; i < addReplays.length; i++) {
        $(addReplays[i]).text(addReplyButtonText);
        $(addReplays[i]).attr("flagAttach", "false");
    }
    attachedPosts = [];
    $("#checkBoxPrivateMessage").prop("checked", false);
    $("#messReplyTable").empty();
    $(".warningText").text("0/1024");
    $("#fileInput").val("");
    $("#tablePrivate table").empty();
    $("#privateBlock").css("display", "none");
}

function sendNewMessageAndBindFiles(files, message) {
    let fileToBase64Promises = [];
    for (let i = 0; i < files.length; i++) {
        fileToBase64Promises.push(fileToBase64(files[i]));
    }
    Promise.all(fileToBase64Promises).then(function () {
        message.files = attachedFilesBase64;
        sendNewMessage(message);
        attachedFilesBase64 = {};
        attachedFiles = [];
        $("#progressBar").css({"display": "none"});
        $("#filesTable").empty();
    }).catch(function (error) {
        alert(error.message);
    });
}
