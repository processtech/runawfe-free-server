import type { WfeChatMessageFile } from '@/domain/wfe-chat-message-file'

export interface WfeChatMessage {
  id: number
  text: string
  author: string
  processId: number
  createDate: string
  files: WfeChatMessageFile[]
}
