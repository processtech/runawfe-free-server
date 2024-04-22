<script setup lang="ts">
import { chatService } from '@/services/chat-service'
import { type WfeChatMessage } from '@/domain/wfe-chat-message'
import { useAuthStore } from '@/stores/auth-store'
import { storeToRefs } from 'pinia'
import { computed, ref, type Ref, type PropType } from 'vue'
import { type FilesContext } from '@/ts/files-context'
import { truncateLabel } from '@/logic/utils'

const authStore = useAuthStore()

const hovering = ref(false)
const editing = ref(false)
const confirmDelete = ref(false)
const text = ref('')
const filesContext: Ref<FilesContext> = ref({ files: {}, valid: true, count: 0 })

const user = storeToRefs(authStore).currentUser

const validEdited = computed(() => {
  return (text.value.trim() || filesContext.value.count > 0)
    && filesContext.value.valid
})

const { message } = defineProps({
  message: {
    type: Object as PropType<WfeChatMessage>,
    required: true,
  }
})

function  deleteMessage(): void {
  chatService.delete(message.id)
}

function editMessage(): void {
  editing.value = false
  chatService.edit({
    id: message.id,
    text: text.value,
    files: filesContext.value.files,
  })
}

function download(id: number, filename: string) {
  chatService.getFile(id).then(blob => {
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = filename
    link.click()
    URL.revokeObjectURL(link.href)
  })
}
</script>

<template>
  <div @mouseover="hovering = true" @mouseleave="hovering = false">
    <v-toolbar density="compact" class="bg-primary-background-darken-1 text-secondary-text">
      <v-row>
        <v-col class="d-flex align-center">
          <v-toolbar-title class="text-caption text-decoration-underline">
            {{ message.author }}
          </v-toolbar-title>
        </v-col>
        <v-col class="d-flex align-center">
          <v-toolbar-title class="text-caption">
            {{ message.createDate?.slice(-6) }}
          </v-toolbar-title>
        </v-col>
        <v-col>
          <v-toolbar-title v-show="hovering && message.author === user?.name">
            <v-btn icon="mdi-pencil-outline" @click="editing = true, text = message.text || ''" />
            <v-btn icon="mdi-delete-outline" @click="confirmDelete = true" />
          </v-toolbar-title>
        </v-col>
      </v-row>
    </v-toolbar>
    <v-card class="text-start text-body-2 py-1 bg-primary-background">
      <v-card-text v-if="!editing">
        {{ message.text }}
        <div class="d-flex justify-end px-4" v-for="file of message.files" :key="file.id">
          <span @click="download(file.id, file.name)">
            <v-chip class="mb-1" link small>
              <v-icon left small>mdi-tray-arrow-down</v-icon>
              <span>{{ truncateLabel(file.name, 25) }}</span>
            </v-chip>
          </span>
        </div>
      </v-card-text>
      <v-card-text v-else>
        <v-textarea class="text-body-2"
          v-model="text"
          auto-grow
          rows="1"
          autofocus
        />
        <v-card-actions class="d-flex justify-end">
        <v-btn-group color="primary" variant="tonal" density="compact">
          <v-btn @click="editing = false">Отмена</v-btn>
          <v-btn @click="editMessage" :disabled="!validEdited">Сохранить</v-btn>
        </v-btn-group>
        </v-card-actions>
      </v-card-text>
    </v-card>
    <v-overlay v-model="confirmDelete" opacity="0.2" class="d-flex justify-center align-center">
      <v-card width="300">
        <v-card-title class="text-center">Удалить сообщение?</v-card-title>
        <v-card-actions class="d-flex justify-space-around">
          <v-btn @click="confirmDelete = false">Отмена</v-btn>
          <v-btn @click="deleteMessage">Удалить</v-btn>
        </v-card-actions>
      </v-card>
    </v-overlay>
  </div>
</template>
