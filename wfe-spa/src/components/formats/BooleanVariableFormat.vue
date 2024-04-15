<template>
  <v-checkbox v-model="model" :disabled="!editing" />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'BooleanVariableFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false
    },
  },

  data: () => ({
    model: false,
  }),

  created() {
    this.model = Boolean(this.variable.value)
  },

  methods: {
    provideNewValue(): boolean {
      return this.model
    },
  },
})
</script>
