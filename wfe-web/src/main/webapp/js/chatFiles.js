let attachedFiles = [];
let attachedFilesBase64 = {};

$(document).ready(function () {
    $("#fileInput").change(function () {
        let files = $(this)[0].files;
        for (let i = 0; i < files.length; i++) {
            attachedFiles.push(files[i]);
            let newFile = $("<tr/>");
            newFile.append($("<td/>").text(attachedFiles[attachedFiles.length - 1].name));
            $("#filesTable").append(newFile);
            this.val = {};
        }
    });
})

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
