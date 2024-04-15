<script setup lang="ts">
import { computed, type ComputedRef, type PropType } from 'vue'
import type { WfeTask } from '@/ts/WfeTask'
import WfeSummary from '@/components/WfeSummary.vue'

const props = defineProps({
  task: {
    required: true,
    type: Object as PropType<WfeTask>,
  },
})

const fields: ComputedRef<{ [key: string]: string }> = computed(() => {
  const {
    definitionName,
    name,
    description,
    processId,
    targetActor,
    swimlaneName,
    createDate,
    deadlineDate
  } = props.task
  return {
    'Имя процесса': definitionName,
    'Имя': name,
    'Описание': description,
    'Номер экземпляра процесса': String(processId),
    'Исполнитель': targetActor ? targetActor.fullName : '',
    'Роль': swimlaneName ? swimlaneName : '',
    'Создана': createDate ? new Date(createDate).toLocaleString() : '',
    'Время окончания': deadlineDate ? new Date(deadlineDate).toLocaleString() : ''
  }
})
</script>

<template>
  <wfe-summary :title="task.name" subtitle="Информация о задаче" :fields="fields" />
</template>
