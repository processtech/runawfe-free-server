import { MessageRequestType } from '@/domain/message-request-type'

export interface ChatMessageRequest {
  messageType: MessageRequestType
  id?: number
  text?: string
  author?: string
  processId?: number
  createDate?: string
  files?: { [fileName: string]: string }
}
