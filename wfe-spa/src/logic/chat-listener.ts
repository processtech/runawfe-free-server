import type { WfeChatMessage } from '@/domain/wfe-chat-message'
import { MessageRequestType } from '@/domain/message-request-type'
import { chatSocket, type ChatSocket } from '@/logic/chat-socket'

class ChatListener {
  constructor(private chatSocket: ChatSocket) {
  }

  onNewMessage(handler: (message: WfeChatMessage) => void): void {
    this.chatSocket.addHandler(MessageRequestType.New, handler)
  }

  onEdit(handler: (message: WfeChatMessage) => void): void {
    this.chatSocket.addHandler(MessageRequestType.Edit, handler)
  }

  onDelete(handler: (message: WfeChatMessage) => void): void {
    this.chatSocket.addHandler(MessageRequestType.Delete, handler)
  }

  // TODO: on error
}

export const chatListener = new ChatListener(chatSocket)
