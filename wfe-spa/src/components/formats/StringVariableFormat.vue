<template>
  <v-text-field v-if="editing" v-model="text" dense />
  <span v-else>{{ variable.value }}</span>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'StringVariableFormat',

  data: () => ({
    text: '',
  }),

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false,
    },
  },

  created() {
    this.text = this.variable.value
  },

  methods: {
    provideNewValue(): string {
      return this.text
    },
  },
})
</script>
