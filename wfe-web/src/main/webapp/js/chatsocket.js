let chatSocket = null;
let currentUser = "";

const ABNORMAL_CLOSURE = 1006;
const MESSAGE_TOO_BIG = 1009;

function initChatSocket(socket) {
    chatSocket = socket;
}

function establishWebSocketConnection(handlers, username) {
    if (!window.localStorage.getItem('runawfe@user')) {
        token().done(function (result) {
            if (!result.token) {
                return;
            }
            window.localStorage.setItem('runawfe@user', JSON.stringify(result));
            initChatSocket(establishWebSocketConnection(handlers, username));
        });
        return;
    }

    currentUser = username;
    let socketProtocol = (document.location.protocol === "https:") ? "wss:" : "ws:";
    let socketUrl = socketProtocol + "//" + document.location.host + "/wfe/chatSocket";
    let socket = new WebSocket(socketUrl);
    socket.binaryType = "arraybuffer";
    // Prevents default behavior of browsers (firefox) to close websocket with code 1001 if page is refreshed
    window.onbeforeunload = function () {
        if (socket.readyState === WebSocket.OPEN) {
            socket.close(1000);
        }
    };

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

        function socketCloseHandler(event) {
            if (event.code === MESSAGE_TOO_BIG) {
                errorMessageAlerter({message: "Превышен максимально допустимый лимит сообщения. Обновите страницу"});
            }
            if (event.code === ABNORMAL_CLOSURE) {
                console.error('chat socket closed')
            }
        }

        socket.onmessage = socketMessageDispatcher;
        socket.onclose = socketCloseHandler;
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
            setTimeout(function () {
                $('#' + notificationId).dialog("close")
            }, 10000);
        }
    });
}

function newMessageAlerter(message) {
    if (message.author === currentUser) {
        sendChatForm();
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

function tokenRespondent(message) {
    if (message.expired) {
        token().done(function (result) {
            if (!result.token) {
                chatSocket.close();
                return;
            }
            window.localStorage.setItem('runawfe@user', JSON.stringify(result));
            sendToken(result.token)
        });
        return;
    }

    var user = JSON.parse(window.localStorage.getItem('runawfe@user'));
    if (!user || !user.token) {
        chatSocket.close();
        return;
    }

    sendToken(user.token)
}

function sendToken(token) {
    var response = {};
    response.payload = token;
    response.messageType = 'tokenMessage';
    sendBinaryMessage(chatSocket, response)
}

function setNotification(message) {
    $("#chatNotificationAlert").text(message);
    $("#chatErrorAlert").empty();
}

function setError(message) {
    $("#chatErrorAlert").text(message);
    $("#chatNotificationAlert").empty();
}

function token() {
    return jQuery.ajax({
        type: "POST",
        url: "/wfe/chatJwtAuth",
        dataType: "json"
    });
}
