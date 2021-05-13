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

function notifyAboutNewMessage(message) {
    $('#notification').remove();
    let messageFrom = document.createElement("h3");
    messageFrom.append(message.author);
    let messageText = (message.text.length < 45) ? message.text : message.text.slice(0, 44) + "...";

    let notification = document.createElement("div");
    notification.setAttribute("id", "notification");
    notification.append(messageFrom, messageText);
    document.body.appendChild(notification);
    $("#notification").dialog({
        buttons: [{
            text: "Перейти в чат",
            click: function () {
                window.location.href = "/wfe/chat_page.do?processId=" + message.processId;
            }
        }],
        title: "Новое сообщение в чате процесса " + message.processId,
        position: ['left', 'bottom'],
        open: function () {
            setTimeout("$('#notification').dialog('close')", 10000);
        }
    });
}

function newMessageAlerter(message) {
    if (message.author === currentUser) {
        setMessageAlert("Сообщение успешно отправлено. Обновите страницу", false);
    } else if (isChatOpen(message)) {
        setMessageAlert("Получено новое сообщение. Обновите страницу", false);
    } else {
        notifyAboutNewMessage(message);
    }
}

function editMessageAlerter(message) {
    if (message.initiator === currentUser) {
        setMessageAlert("Сообщение успешно отредактированно. Обновите страницу", false);
    } else if (isChatOpen(message)) {
        setMessageAlert("Одно из сообщений было изменено. Обновите страницу", false);
    }
}

function deleteMessageAlerter(message) {
    if (message.initiator === currentUser) {
        setMessageAlert("Сообщение успешно удалено. Обновите страницу", false);
    } else if (isChatOpen(message)) {
        setMessageAlert("Одно из сообщений было удалено. Обновите страницу", false);
    }
}

function errorMessageAlerter(message) {
    setMessageAlert("Ошибка при отправке сообщения: " + message.message, true);
}

function setMessageAlert(message, error) {
    let alertMessageDiv;
    if (error) {
        alertMessageDiv = document.getElementById("errorAlert");
        alertMessageDiv.innerHTML = "";
    } else {
        alertMessageDiv = document.getElementById("messageAlert");
        alertMessageDiv.innerHTML = "";
    }
    alertMessageDiv.append(message);
}