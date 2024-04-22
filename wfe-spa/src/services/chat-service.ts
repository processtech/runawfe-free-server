import { apiClient } from '@/logic/api-client'
import { chatSocket } from '@/logic/chat-socket'
import type { ChatSocket } from '@/logic/chat-socket'
import type { WfeChatMessage } from '@/domain/wfe-chat-message'
import type { WfeChatRoom } from '@/domain/wfe-chat-room'
import { MessageRequestType } from '@/domain/message-request-type'

class ChatService {
  constructor(private chatSocket: ChatSocket) {
  }

  async getChatRooms(): Promise<WfeChatRoom[]> {
    const client = await apiClient()
    const data = await client['chat-controller'].getChatRoomsUsingGET()
    return data.body.map((item: any): WfeChatRoom => ({
      newMessagesCount: item.newMessagesCount,
      processId: item.process.id,
      processName: item.process.name,
    }));
  }

  async getMessagesByProcessId(processId: number): Promise<WfeChatMessage[]> {
    const client = await apiClient()
    const data = await client['chat-controller'].getChatMessagesUsingGET({ processId })
    return data.body
  }

  send(message: {
    text: string,
    author: string,
    createDate: string,
    processId: number,
    files?: { [filename: string]: string },
  }): void {
    this.chatSocket.send({ ...message, messageType: MessageRequestType.New })
  }

  edit(message: { id: number, text: string, files?: { [name: string]: string }}): void {
    this.chatSocket.send({ ...message, messageType:  MessageRequestType.Edit })
  }

  delete(id: number) {
    this.chatSocket.send({ id, messageType: MessageRequestType.Delete })
  }

  async getFile(fileId: number): Promise<Blob> {
    const client = await apiClient()
    const response = await client['chat-controller'].getFileUsingGET({ fileId })
    return response.text
  }
}

export const chatService = new ChatService(chatSocket)
