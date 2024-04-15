<template>
  <v-select
    v-model="selected"
    :items="options"
    density="compact"
    outlined
    clearable
    hide-details="auto"
    @update:modelValue="$emit('updateInput', filterFormat)"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { escapeFilterString, unescapeFilterString } from '../../logic/utils'

export default defineComponent({
  name: 'OptionsFilterFormat',

  expose: ['clear'],

  props: {
    options: Object,
    initValue: String,
  },

  data: () => ({
    selected: '',
  }),

  computed: {
    filterFormat(): string {
      return escapeFilterString(this.selected)
    },
  },

  created() {
    this.selected = unescapeFilterString(this.initValue)
  },

  methods: {
    clear(): void {
      this.selected = ''
    },
  },
})
</script>
