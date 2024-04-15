<template>
  <span class="date-time-input">
    <v-text-field
      v-model="start"
      type="datetime-local"
      density="compact"
      outlined
      clearable
      hide-details
      @update:modelValue="$emit('updateInput', filterFormat)"
      :style="{ 'opacity': start ? 1 : 0.5 }"
    />
    <v-text-field
      v-model="end"
      type="datetime-local"
      density="compact"
      outlined
      clearable
      hide-details
      @update:modelValue="$emit('updateInput', filterFormat)"
      :style="{ 'opacity': end ? 1 : 0.5 }"
    />
  </span>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { datePickerFormatToIso, dateIsoToDatePickerFormat } from '../../logic/utils'

export default defineComponent({
  name: 'DateTimeFilterFormat',

  expose: ['clear'],

  props: {
    initValue: String,
  },

  data: () =>({
    start: '',
    end: '',
  }),

  computed: {
    filterFormat(): string {
      const start = dateIsoToDatePickerFormat(this.start)
      const end = dateIsoToDatePickerFormat(this.end)
      if (!start && !end) {
        return ''
      }
      return [start, end].join('|')
    },
  },

  created() {
    if (!this.initValue) {
      return
    }
    const [start, end] = this.initValue.split('|')
    this.start = datePickerFormatToIso(start)
    this.end = datePickerFormatToIso(end)
  },

  methods: {
    clear(): void {
      this.start = ''
      this.end = ''
    },
  },
})
</script>
