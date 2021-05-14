let chatSocket = null;
let currentUser = "";

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
        setNotification("Сообщение успешно отправлено. Обновите страницу");
    } else if (isChatOpen(message)) {
        setNotification("Получено новое сообщение. Обновите страницу");
    } else {
        notifyAboutNewMessage(message);
    }
}

function editMessageAlerter(message) {
    if (message.initiator === currentUser) {
        setNotification("Сообщение успешно отредактированно. Обновите страницу");
    } else if (isChatOpen(message)) {
        setNotification("Одно из сообщений было изменено. Обновите страницу");
    }
}

function deleteMessageAlerter(message) {
    if (message.initiator === currentUser) {
        setNotification("Сообщение успешно удалено. Обновите страницу");
    } else if (isChatOpen(message)) {
        setNotification("Одно из сообщений было удалено. Обновите страницу");
    }
}

function errorMessageAlerter(message) {
    setError("Ошибка при отправке сообщения: " + message.message);
}

function setNotification(message) {
    $("#chatNotificationAlert").text(message);
}

function setError(message) {
    $("#chatErrorAlert").text(message);
}
