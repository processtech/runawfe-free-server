<template>
  <div>
    <v-text-field
      type="number"
      placeholder="от"
      v-model="start"
      density="compact"
      clearable
      outlined
      hide-details
      hide-spin-buttons
      @update:modelValue="$emit('updateInput', filterFormat)"
    />
    <v-text-field
      type="number"
      placeholder="до"
      v-model="end"
      density="compact"
      clearable
      outlined
      hide-details
      hide-spin-buttons
      @update:modelValue="$emit('updateInput', filterFormat)"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'LongFilterFormat',

  expose: ['clear'],

  data: () => ({
    start: '',
    end: '',
  }),

  computed: {
    filterFormat(): string {
      if (!this.start && !this.end) {
        return ''
      }
      return [this.start, this.end].join('-')
    },
  },

  props: {
    initValue: String,
  },

  created() {
    const [start, end] = (this.initValue || '').split('-')
    this.start = start || ''
    this.end = end || ''
  },

  methods: {
    clear(): void {
      this.start = ''
      this.end = ''
    },
  },
})
</script>
