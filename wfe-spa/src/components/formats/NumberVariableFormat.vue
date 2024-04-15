<template>
  <v-text-field v-if="editing" v-model="integer" single-line type="number" />
  <span v-else>{{ variable.value }}</span>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'NumberVariableFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false,
    },
  },

  data: () => ({
    integer: undefined as number | undefined
  }),

  created() {
    this.integer = this.variable.value
  },

  methods: {
    provideNewValue(): number {
      return this.integer
    },
  },
})
</script>
