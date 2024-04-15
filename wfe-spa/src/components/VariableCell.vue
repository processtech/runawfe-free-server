<script setup lang="ts">
import { type WfeVariable } from '@/ts/WfeVariable'
import { VariableContext } from '@/logic/variable-context'
import { removeListElementFormatIfPresent } from '@/logic/utils'
import { onMounted, onUpdated, ref } from 'vue'

const props = defineProps<{
  variable: WfeVariable | undefined,
}>()

const componentName = ref('')

onMounted(initComponent)
onUpdated(initComponent)

function initComponent() {
  if (props.variable) {
    const rootFormat = removeListElementFormatIfPresent(props.variable.format)
    componentName.value = new VariableContext(rootFormat).componentName()
  }
}
</script>

<template>
  <div v-if="componentName">
    <component :is="componentName" :variable="variable" />
  </div>
</template>
