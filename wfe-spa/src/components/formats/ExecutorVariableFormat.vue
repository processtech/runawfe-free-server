<template>
  <div>
    <v-select v-if="editing"
      :items="executors"
      v-model="currentExecutor"
      item-text="name"
      :value="variable.value"
      :return-object="true"
    />
    <!-- TODO should be a link to the executor -->
    <span v-else>{{ variable.value.name }}</span>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { executorService } from '../../services/executor-service'
import { WfeExecutor } from '../../ts/WfeExecutor'
import { PropType } from 'vue'
import { ExecutorType } from '../../ts/ExecutorType'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'ExecutorVariableFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false
    },
  },

  data: () => ({
    currentExecutor: {} as WfeExecutor,
    executors: [] as WfeExecutor[],
  }),

  created() {
    executorService.getExecutorsByType(ExecutorType.Executor)
      .then(exs => this.executors = exs)
      .then(exs => this.currentExecutor = exs.find(ex => ex.id == this.variable.value.id))
  },

  methods: {
    provideNewValue(): string {
      return this.currentExecutor.name
    },
  },
})
</script>
