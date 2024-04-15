<script setup lang="ts">
import { computed, ref, type PropType, type Ref, type ComputedRef, onMounted } from 'vue'
import { wfeRouter } from '../logic/wfe-router'
import { processService } from '../services/process-service'

const props = defineProps({
  processIds: {
    type: Array as PropType<number[]>,
    required: true,
  },
})

const search = ref('')
const suggestions: Ref<string[]> = ref([])

const visibleColumns: ComputedRef<string[]> = computed(() => wfeRouter.queryArray('visible'))
const variables: ComputedRef<string[]> = computed(() => wfeRouter.queryArray('vars'))

onMounted(async () => {
  suggestions.value = await processService.getVariableNames(props.processIds)
})

function addVariable(): void {
  if (!search.value || variables.value.includes(search.value)) {
    return
  }
  const vars = [ ...variables.value ]
  const visible = [ ...visibleColumns.value ]
  vars.push(search.value)
  visible.push(search.value)
  wfeRouter.mergeQueryParams({ vars, visible })
  search.value = ''
}
</script>

<template>
  <v-combobox
    :items="suggestions"
    placeholder="Добавить переменную"
    density="compact"
    @update:modelValue="v => search = String(v)"
  >
    <template v-slot:append>
      <v-btn
        color="primary"
        variant="tonal"
        prepend-icon="mdi-plus"
        @click="addVariable"
        height="2.6rem"
      >
        Добавить
      </v-btn>
    </template>
  </v-combobox>
</template>
