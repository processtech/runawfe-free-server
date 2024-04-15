<template>
  <expanding-cell>
    <v-textarea v-if="editing" v-model="text" rows="3" full-width />
    <span v-else v-html="(variable.value || '').replaceAll('\n', '<br />')" />
  </expanding-cell>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'TextVaraibleFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false,
    },
  },

  data: () => ({
    text: '',
  }),

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
