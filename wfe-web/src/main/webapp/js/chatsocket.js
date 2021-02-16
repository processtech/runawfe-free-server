let chatSocket = null;

function initChatSocket(socket) {
    chatSocket = socket;
}

function establishWebSocketConnection(handlers) {
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

function newMessageAlerter(message) {
    console.log(message);
    alert("New Message!");
}

function editMessageAlerter(message) {
    console.log(message);
    alert("Edit Message!");
}

function errorMessageAlerter(message) {
    alert("Сообщение не отправлено. Error: " + message.message);
}

function deleteMessageAlerter(message) {
    console.log(message);
    alert("Delete Message!");
}
