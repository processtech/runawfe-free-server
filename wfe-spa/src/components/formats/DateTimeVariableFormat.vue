<template>
  <v-container v-if="editing">
    <v-row>
      <v-col>
        <v-menu transition="scale-transition" min-width="auto">
          <template v-slot:activator="{ on }">
            <v-text-field v-model="dateIso" prepend-icon="mdi-calendar" readonly v-on="on" />
          </template>
          <v-date-picker no-title v-model="dateIso" @input="showDatePicker = false" />
        </v-menu>
      </v-col>
      <v-col>
        <v-menu
          ref="menu"
          v-model="showTimePicker"
          :close-on-content-click="false"
          :return-value.sync="timeIso"
          transition="scale-transition"
        >
          <template v-slot:activator="{ on }">
            <v-text-field
              v-model="timeIso"
              prepend-icon="mdi-clock-time-four-outline"
              readonly
              v-on="on"
            />
          </template>
					<!--
          <v-time-picker
            v-if="showTimePicker"
            v-model="timeIso"
            @click:minute="$refs.menu.save(timeIso)"
          />
					-->
        </v-menu>
      </v-col>
    </v-row>
  </v-container>
  <span v-else>
    {{ formatDateTime(new Date(variable.value)) }}
  </span>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { formatDateTime } from '../../logic/utils'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'DateTimeVariableFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false
    },
  },

  data: () => ({
    dateIso: '',
    timeIso: '',
    showDatePicker: false,
    showTimePicker: false,
  }),

  created() {
    const value = this.variable.value
    this.timezone = value.slice(-5)
    this.dateIso = value.slice(0, 10)
    this.timeIso = value.slice(11, 16)
  },

  methods: {
    formatDateTime,

    provideNewValue(): string {
      return this.dateIso + 'T' + this.timeIso + this.timezone
    },
  },
})
</script>
