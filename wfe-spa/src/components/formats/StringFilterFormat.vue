<template>
  <v-text-field
    v-model="value"
    density="compact"
    hide-details
    clearable
    placeholder="Содержит"
    @update:modelValue="$emit('updateInput', filterFormat)"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { escapeFilterString, unescapeFilterString } from '../../logic/utils'

export default defineComponent({
  name: 'StringFilterFormat',

  expose: ['clear'],

  props: {
    initValue: String,
  },

  data: () => ({
    value: '',
  }),

  computed: {
    filterFormat(): string {
      return escapeFilterString(this.value)
    },
  },

  created() {
    this.value = unescapeFilterString(this.initValue)
  },

  methods: {
    clear(): void {
      this.value = ''
    },
  },
})
</script>
