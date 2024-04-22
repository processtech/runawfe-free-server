<script setup lang="ts">
import { computed, type PropType, type Ref } from 'vue'
import WfeSummary from '@/components/WfeSummary.vue'
import type { WfeProcess } from '@/ts/WfeProcess'
import { usePreferencesStore } from '@/stores/preferencese-store'

const preferencesStore = usePreferencesStore()

const props = defineProps({
  process: {
    type: Object as PropType<WfeProcess>,
    required: false,
  },
  showChatButton: {
    type: Boolean,
    default: false,
  },
})

const fields: Ref<{ [key: string]: string | undefined }> = computed(() => {
  if (!props.process) {
    return {}
  }
  const { id, definitionName, startDate, endDate, executionStatus } = props.process
  return {
    'Номер': String(id),
    'Имя процесса': definitionName,
    'Запущен': startDate ? new Date(startDate).toLocaleString() : '',
    'Завершён': endDate ? new Date(endDate).toLocaleString() : '',
    'Статус': executionStatus
  }
})
</script>

<template>
  <wfe-summary
    :title="process?.definitionName"
    subtitle="Информация об экземпляре "
    :fields="fields"
  >
    <v-btn
      v-if="props.showChatButton && preferencesStore.showChat"
      color="primary"
      variant="tonal"
      @click="$router.push(`/chat/${props.process?.id}/card`)"
    >
      Чат процесса
    </v-btn>
  </wfe-summary>
</template>
