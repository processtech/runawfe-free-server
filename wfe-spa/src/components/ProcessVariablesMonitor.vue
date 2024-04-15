<script setup lang="ts">
import { ref, onMounted, type Ref } from 'vue'
import { type WfeVariable } from '@/ts/WfeVariable'
import { processService } from '@/services/process-service'

const props = defineProps({
  processId: {
    type: Number,
    required: true,
  },
})

const variables: Ref<WfeVariable[]> = ref([])

onMounted(updateVariables)

// TODO lists are not updated reactivly
function updateVariables(): void {
  setTimeout(() => processService.getProcessVariables(props.processId)
    .then(vs => variables.value = vs), 100) // TODO try to implement without setTimeout
}
</script>

<template>
  <div v-if="variables.length">
    <h3 class="text-right py-3 text-primary-text">Переменные процесса</h3>
    <v-card class="rounded-0">
      <v-table id="variables" density="compact" class="bg-primary-background">
        <thead>
          <tr>
            <th
              class="subtitle-2 text-medium-emphasis"
              width="200"
              style="min-width: 200px"
             >
               Имя
            </th>
            <th
              class="subtitle-2 text-medium-emphasis"
              width="150"
              style="min-width: 150px"
            >
              Формат
            </th>
            <th class="subtitle-2 text-medium-emphasis">Значение</th>
          </tr>
        </thead>
        <tbody>
          <variable-row v-for="variable in variables"
            :key="variable.name"
            :variable="variable"
            @saveVariable="updateVariables"
          />
        </tbody>
      </v-table>
    </v-card>
  </div>
</template>
