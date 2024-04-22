<script setup lang="ts">
import WfeTable from '@/components/WfeTable.vue'
import { type WfeChatRoom } from '@/domain/wfe-chat-room'
import { chatService } from '@/services/chat-service'
import { onMounted, ref, type Ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const chatRooms: Ref<WfeChatRoom[]> = ref([])
const headers = ref([
  {
    title: 'Идентификатор процесса',
    value: 'processId',
  },
  {
    title: 'Процесс',
    value: 'processName',
  },
  {
    title: 'Непрочитанных сообщений',
    value: 'newMessagesCount',
  },
])

onMounted(async () => {
  chatRooms.value = await chatService.getChatRooms()
})

function openChat(_event: MouseEvent, row: { item: WfeChatRoom }): void {
  router.push(`/chat/${row.item.processId}/card/`)
}

function rowProps(row: { item: WfeChatRoom }): { class: string } {
  return {
    class: row.item.newMessagesCount > 0 ? 'font-weight-bold' : ''
  }
}
</script>

<template>
  <wfe-table>
    <v-data-table
      class="elevation-1 wfe-process-table"
      :row-props="rowProps"
      :headers="headers"
      :items="chatRooms"
      :sort-by="[{ key: 'newMessagesCount' }]"
      :sort-desc="true"
      @click:row="openChat"
    />
  </wfe-table>
</template>
