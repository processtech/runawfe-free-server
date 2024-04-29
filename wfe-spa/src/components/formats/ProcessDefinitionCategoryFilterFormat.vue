<template>
  <v-autocomplete
    v-model="selected"
    :items="categories"
    label="Категория"
    density="compact"
    outlined
    clearable
    hide-details="auto"
    @update:modelValue="$emit('updateInput', filterFormat)"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { processDefinitionService } from '@/services/process-definition-service'

export default defineComponent({
  name: 'ProcessDefinitionCategoryFilterFormat',

  expose: ['clear'],

  props: {
    options: Object,
    initValue: String,
  },

  data: () => ({
    categories: [],
    selected: '',
  }),

  computed: {
    filterFormat(): string {
      return this.selected
    },
  },

  created() {
    this.selected = this.initValue
    this.loadCategories();
  },

  methods: {
    loadCategories() {
      processDefinitionService.getCategories().then(categories => this.categories = categories)
    },

    clear(): void {
      this.selected = ''
    },
  },
})
</script>
