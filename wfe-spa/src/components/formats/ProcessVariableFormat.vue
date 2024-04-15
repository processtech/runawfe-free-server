<template>
  <!-- TODO use <v-select> or validate process exists -->
  <v-text-field v-if="editing" v-model="processId" single-line type="number" />
  <router-link v-else :to="`/process/${variable.value}/card`">{{ variable.value }}</router-link>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'ProcessVariableFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false,
    },
  },

  created() {
    this.processId = this.variable.value
  },

  methods: {
    provideNewValue(): number {
      return this.processId
    },
  },
})
</script>
