<script setup lang="ts">
import { type Ref, type PropType, computed } from 'vue'
import type { WfeProcessDefinition } from '@/ts/WfeProcessDefinition'
import WfeSummary from '@/components/WfeSummary.vue'

const props = defineProps({
  processDefinition: {
    required: true,
    type: Object as PropType<WfeProcessDefinition>,
  },
})

const fields: Ref<{ [name: string]: string }> = computed(() => {
  const {
    id, name, description, createDate, createActor, updateDate, updateActor
  } = props.processDefinition
  return {
    'Id': String(id),
    'Имя': name,
    'Описание': description,
    'Создан': createDate ? new Date(createDate).toLocaleString() : '',
    'Автор создания': createActor ? createActor.fullName : '',
    'Обновлен': updateDate ? new Date(updateDate).toLocaleString() : '',
    'Автор обновления': updateActor ? updateActor.fullName : '',
  }
})
</script>

<template>
  <wfe-summary :title="processDefinition?.name" subtitle="Информация о процессе" :fields="fields" />
</template>
