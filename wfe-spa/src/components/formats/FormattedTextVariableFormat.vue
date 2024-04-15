<template>
  <expanding-cell>
    <template>
      <div v-html="variable.value" />
    </template>
  </expanding-cell>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'FormattedTextVariableFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false,
    },
  },
})
</script>
