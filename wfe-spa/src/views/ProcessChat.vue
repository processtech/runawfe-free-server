<template>
  <v-card class="pa-8 bg-primary-background">
    <process-summary :process="process" />
    <div class="chat-wrapper">
      <v-card class="my-3 pa-2 bg-primary-background elevation-0" width="50%">
        <v-textarea class="px-2 pt-1"
          label="Новое сообщение"
          auto-grow
          clearable
          v-model="newMessage"
        />
        <v-card-actions class="d-flex justify-end">
          <file-input @inputFiles="updateFiles" ref="fileInput" />
          <v-btn color="primary" variant="tonal" @click="sendNewMessage" :disabled="!valid"  size="large">
            <v-icon>mdi-send</v-icon>
          </v-btn>
        </v-card-actions>
      </v-card>
      <v-card
        class="text-center bg-primary-background"
        v-for="(messages, date) in messagesByDate"
        :key="date"
        width="75%"
      >
        <v-chip class="my-5 bg-primary">{{ date }}</v-chip>
        <chat-message v-for="message in messages" :key="message.id" :message="message" />
      </v-card>
    </div>
  </v-card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import type { WfeChatMessage } from '@/domain/wfe-chat-message'
import { chatService } from '@/services/chat-service'
import { groupBy } from 'lodash'
import { formatDate } from '@/logic/utils'
import { useAuthStore } from '@/stores/auth-store'
import { mapState } from 'pinia'
import ChatMessage from '@/components/ChatMessage.vue'
import ProcessSummary from '@/components/ProcessSummary.vue'
import FileInput from '@/components/FileInput.vue'
import type { WfeProcess } from '@/ts/WfeProcess'
import { processService } from '@/services/process-service'
import { chatListener } from '@/logic/chat-listener'
import type { FilesContext } from '@/ts/files-context'

export default defineComponent({
  components: { ChatMessage, ProcessSummary, FileInput },

  name: 'ProcessChat',

  data: () => ({
    messages: [] as WfeChatMessage[],
    newMessage: '' as string,
    process: {} as WfeProcess,
    filesContext: { files: {}, valid: true, count: 0 },
  }),

  computed: {
    ...mapState(useAuthStore, { user: 'currentUser' }),

    messagesByDate(): { [date: string] : WfeChatMessage[] } {
      return groupBy(this.messages, message => {
        return message.createDate?.slice(0, -6); // cut time of date string
      });
    },

    valid(): boolean {
      return (Boolean(this.newMessage?.trim()) || this.filesContext.count > 0)
        && this.filesContext.valid
    },
  },

  created() {
    const processId = Number(this.$route.params.processId);
    processService.getProcess(processId)
      .then(p => this.process = p)
    chatService.getMessagesByProcessId(processId)
      .then(ms => this.messages = ms);
    chatListener.onNewMessage(message => {
      if (processId === message.processId) {
        this.messages.unshift(message)
      }
    });
    chatListener.onEdit(message => {
      const edited = this.messages.find(m => message.id === m.id)
      if (edited) {
        edited.text = message.text
      }
    });
    chatListener.onDelete(message => {
      this.messages = this.messages.filter(m => m.id !== message.id)
    });
  },

  methods: {
    sendNewMessage(): void {
      chatService.send({
        text: this.newMessage.trim(), /* bug with line breaks when editing message in old ui */
        author: this.user?.name,
        processId: Number(this.$route.params.processId),
        createDate: formatDate(new Date),
        files: this.filesContext.files,
      });
      this.newMessage = ''
      // @ts-ignore
      this.$refs.fileInput.clear()
    },

    updateFiles(fc: FilesContext): void {
      this.filesContext = fc
    },

    updateMessages(): void {
      const processId = Number(this.$route.params.processId)
      chatService.getMessagesByProcessId(processId)
        .then(ms => this.messages = ms)
    },
  },

})
</script>

<style lang="scss">
  .chat-wrapper {
    max-width: 1200px;
  }
</style>
