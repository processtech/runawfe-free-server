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

function newMessageAlerter(message) {
    console.log(message);
    if (message.author === currentUser) {
        if (confirm("Сообщение успешно отправлено! Хотите перезагрузить страницу?")) {
            location.reload();
        }
    } else if (isChatOpen(message)) {
        if (confirm("Вы получили новое сообщение! Хотите перезагрузить страницу?")) {
            location.reload();
        }
    } else {
        alert("Вы получили новое сообщение!");
    }
}

function editMessageAlerter(message) {
    if (message.author === currentUser) {
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
    if (message.author === currentUser) {
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
