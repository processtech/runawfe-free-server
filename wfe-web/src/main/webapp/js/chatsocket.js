let chatSocket = null;
let currentUser = "";
let isConfirmDialogClosed = true;

function initChatSocket(socket) {
    chatSocket = socket;
}

function establishWebSocketConnection(handlers, username) {
    currentUser = username;
    let socketProtocol = (document.location.protocol === "https:") ? "wss:" : "ws:";
    let socketUrl = socketProtocol + "//" + document.location.host + "/wfe/chatSocket";
    let socket = new WebSocket(socketUrl);
    socket.binaryType = "arraybuffer";

    if (handlers) {
        function socketMessageDispatcher(event) {
            let message = JSON.parse(event.data);
            if (!message) {
                return;
            }

            let handler = handlers[message.messageType];
            if (handler) {
                handler(message);
            }
        }

        socket.onmessage = socketMessageDispatcher;
    }

    return socket;
}

function sendBinaryMessage(socket, message) {
    let encoder = new TextEncoder();
    let bytes = encoder.encode(JSON.stringify(message));
    socket.send(bytes);
}

function isChatOpen(message) {
    return window.location.pathname === "/wfe/chat_page.do"
        && window.location.search === "?processId=" + message.processId;
}

let aliveNotification = "";

function notifyAboutNewMessage(message) {
    $('#' + aliveNotification).remove();
    let notificationId = 'notification' + message.id.toString();
    aliveNotification = notificationId;

    let messageFrom = document.createElement("h3");
    messageFrom.append(message.author);
    let messageText = (message.text.length < 45) ? message.text : message.text.slice(0, 44) + "...";

    let notification = document.createElement("div");
    notification.setAttribute("id", notificationId);
    notification.append(messageFrom, messageText);
    document.body.appendChild(notification);
    $('#' + notificationId).dialog({
        buttons: [{
            text: "Перейти в чат",
            click: function () {
                window.location.href = "/wfe/chat_page.do?processId=" + message.processId;
            }
        }],
        title: "Новое сообщение в чате процесса " + message.processId,
        position: ['left', 'bottom'],
        open: function () {
            setTimeout(function () {$('#' + notificationId).dialog("close")}, 10000);
        }
    });
}

function newMessageAlerter(message) {
    if (message.author === currentUser) {
        if (confirm("Сообщение успешно отправлено! Хотите перезагрузить страницу?")) {
            location.reload();
        }
    } else if (isChatOpen(message)) {
        if (isConfirmDialogClosed) {
            isConfirmDialogClosed = false;
            if (confirm("Вы получили новое сообщение! Хотите перезагрузить страницу?")) {
                location.reload();
            }
        }
    } else {
        notifyAboutNewMessage(message);
    }
}

function editMessageAlerter(message) {
    if (message.initiator === currentUser) {
        if (confirm("Сообщение успешно отредактировано! Хотите перезагрузить страницу?")) {
            location.reload();
        }
    } else if (isChatOpen(message)) {
        if (confirm("Одно из сообщений было отредактировано! Хотите перезагрузить страницу?")) {
            location.reload();
        }
    }
}

function deleteMessageAlerter(message) {
    if (message.initiator === currentUser) {
        if (confirm("Сообщение успешно удалено! Хотите перезагрузить страницу?")) {
            location.reload();
        }
    } else if (isChatOpen(message)) {
        if (confirm("Одно из сообщений было удалено! Хотите перезагрузить страницу?")) {
            location.reload();
        }
    }
}

function errorMessageAlerter(message) {
    alert("Сообщение не отправлено. Ошибка: " + message.message);
}
