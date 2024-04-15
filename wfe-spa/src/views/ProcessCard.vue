<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { processService } from '../services/process-service'
import { useRoute } from 'vue-router'
import ProcessSummary from '../components/ProcessSummary.vue'
import ProcessVariablesMonitor from '../components/ProcessVariablesMonitor.vue'

const route = useRoute()
const graphImage = ref('')
const showGraph = ref(false)

onMounted(() => {
  processService.getProcessGraph(Number(route.params.id))
    .then(gi => graphImage.value = gi)
})
</script>

<template>
  <v-card flat class="bg-primary-background pb-6">
    <process-summary :processId="Number($route.params.id)" :showChatButton="false" />
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
