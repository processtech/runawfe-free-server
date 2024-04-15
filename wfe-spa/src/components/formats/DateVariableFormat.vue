<template>
  <v-menu v-if="editing" transition="scale-transition" min-width="auto">
    <template v-slot:activator="{ on }">
      <v-text-field v-model="dateIso" prepend-icon="mdi-calendar" readonly v-on="on" />
    </template>
    <v-date-picker v-model="dateIso" @input="showPicker = false" no-title />
  </v-menu>
  <span v-else>
    {{ formatDate(new Date(variable.value)) }}
  </span>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { formatDate } from '../../logic/utils'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'DateVariableFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false
    },
  },

  data: () => ({
    dateIso: '',
    showPicker: false,
  }),

  created() {
    this.dateIso = this.variable.value.substring(0, 10)
  },

  methods: {
    formatDate,

    provideNewValue(): string {
      return this.dateIso
    },
  },
})
</script>
