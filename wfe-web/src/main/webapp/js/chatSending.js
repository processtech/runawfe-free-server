let newMessageType = "newMessage";
let editMessageType = "editMessage";
let deleteMessageType = "deleteMessage";
let readMessageType = "readMessage";

let editMessageId = -1;

let attachedFiles = [];
let attachedFilesBase64 = {};
let fileInp = 1024 * 1024 * 20;

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

function fileToBase64(file) {
    return new Promise(function (resolve, reject) {
        let reader = new FileReader();
        let buffer = new ArrayBuffer();
        reader.onload = function (e) {
            buffer = e.target.result;
            attachedFilesBase64[file.name] = btoa(buffer);
            resolve();
        }
        reader.onerror = function (error) {
            reject(error);
        }
        reader.readAsBinaryString(file)
    });
}

//альтернатива - fileInput
$("#file").change(function () {
    console.info("FILE!")
    let files = $(this)[0].files;
    for (let i = 0; i < files.length; i++) {
        let fileSize = ("size" in files[i]) ? files[i].size : files[i].fileSize;
        if (fileSize < fileInp) {
            attachedFiles.push(files[i]);
            let newFile = $("<tr/>");
            newFile.append($("<td/>").text(attachedFiles[attachedFiles.length - 1].name));
            let deleteFileButton = $("<button/>");
            deleteFileButton.text("X");
            deleteFileButton.addClass("btnFileChat");
            deleteFileButton.attr("fileNumber", attachedFiles.length - 1);
            deleteFileButton.attr("type", "button");
            deleteFileButton.click(deleteAttachedFile);
            newFile.append($("<td/>").append(deleteFileButton));
            $("#filesTable").append(newFile);
        }
        else {
            alert("errorMessFilePart1" + (fileSize - fileInp) + "errorMessFilePart2" + fileInp / (1073741824) + " Gb / " + fileInp + "bite");
        }
        this.val = {};
    }
});

function deleteAttachedFile() {
    attachedFiles.splice($(this).attr("fileNumber"));
    $(this).closest("tr").remove();
    return false;
}
