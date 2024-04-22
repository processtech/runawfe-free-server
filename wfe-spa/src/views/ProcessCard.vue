<script setup lang="ts">
import { onMounted, ref, type Ref } from 'vue'
import { processService } from '../services/process-service'
import { useRoute } from 'vue-router'
import ProcessSummary from '../components/ProcessSummary.vue'
import ProcessVariablesMonitor from '../components/ProcessVariablesMonitor.vue'
import type { WfeProcess } from '@/ts/WfeProcess'

const route = useRoute()

const graphImage = ref('')
const showGraph = ref(false)
const process: Ref<WfeProcess | undefined> = ref(undefined)

onMounted(async () => {
  graphImage.value = await processService.getProcessGraph(Number(route.params.id))
  process.value = await processService.getProcess(Number(route.params.id))
})
</script>

<template>
  <v-card flat class="bg-primary-background pb-6">
    <process-summary :process="process" :showChatButton="true" />
    <process-variables-monitor :processId="Number($route.params.id)" class="px-4 pb-4" />
    <v-row justify="center">
      <v-dialog
        v-model="showGraph"
        fullscreen
        transition="dialog-bottom-transition"
        close-on-content-click
      >
        <template v-slot:activator="{ props }">
          <v-btn v-bind="props" color="primary" flat block class="mt-3">Граф процесса</v-btn>
        </template>
        <div class="mx-auto my-auto overflow-x-auto">
          <img :src="graphImage" />
        </div>
      </v-dialog>
    </v-row>
  </v-card>
</template>
