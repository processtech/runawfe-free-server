<script setup lang="ts">
import { computed, onMounted, ref, type Ref } from 'vue'
import { processService } from '@/services/process-service'
import WfeSummary from '@/components/WfeSummary.vue'
import { useRoute } from 'vue-router'
import type { WfeProcess } from '@/ts/WfeProcess';

const route = useRoute()

const process: Ref<WfeProcess | undefined> = ref(undefined)

const fields: Ref<{ [key: string]: string | undefined }> = computed(() => {
  if (!process.value) {
    return {}
  }
  const { id, definitionName, startDate, endDate, executionStatus } = process.value
  return {
    'Номер': String(id),
    'Имя процесса': definitionName,
    'Запущен': startDate ? new Date(startDate).toLocaleString() : '',
    'Завершён': endDate ? new Date(endDate).toLocaleString() : '',
    'Статус': executionStatus
  }
})

onMounted(async () => {
  process.value = await processService.getProcess(Number(route.params.id))
})
</script>

<template>
  <wfe-summary
    :title="process?.definitionName"
    subtitle="Информация об экземпляре "
    :fields="fields"
  />
</template>
