import { MessageRequestType } from '@/domain/message-request-type'
import { systemConfiguration } from '@/logic/system-configuration'
import type { ChatMessageRequest } from '@/domain/chat-message-request'
import type { WfeChatMessage } from '@/domain/wfe-chat-message'

class ChatSocketImpl implements ChatSocket {
  private readonly handlers: { [key in MessageRequestType]: ((message: WfeChatMessage) => void)[] }
  private readonly textEncoder: TextEncoder
  private readonly webSocket: WebSocket

  constructor() {
    this.handlers = {
      [MessageRequestType.New]: [],
      [MessageRequestType.Edit]: [],
      [MessageRequestType.Delete]: [],
      [MessageRequestType.Error]: [],
      [MessageRequestType.Auth]: [],
    }
    this.textEncoder = new TextEncoder()
    this.webSocket = new WebSocket(systemConfiguration.webSocketUrl() + '/wfe/chatSocket')
    this.initSocket()
  }

  addHandler(messageType: MessageRequestType, handler: (message: WfeChatMessage) => void): void {
    this.handlers[messageType].push(handler)
  }

  send(message: ChatMessageRequest): void {
    this.webSocket.send(this.textEncoder.encode(JSON.stringify(message)))
  }

  private initSocket() {
    this.webSocket.binaryType = 'arraybuffer'
    this.webSocket.onmessage = event => {
      const data = JSON.parse(event.data)
      const messageType = data.messageType as MessageRequestType
      this.handlers[messageType].forEach(h => h(data))
    };
    this.handlers[MessageRequestType.Auth].push((_data) => {
      // TODO: case, when token is expired?
      const response = {
        payload: JSON.parse(localStorage.getItem('runawfe@user') || '{}').token,
        messageType: 'tokenMessage',
      };
      this.webSocket.send(this.textEncoder.encode(JSON.stringify(response)))
    })
  }
}

export interface ChatSocket {
  addHandler(messageType: MessageRequestType, handler: (message: WfeChatMessage) => void): void
  send(message: ChatMessageRequest): void
}

export const chatSocket = new ChatSocketImpl()
