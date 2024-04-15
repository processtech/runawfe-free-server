<template>
  <div v-if="editing">
    <v-menu
      ref="menu"
      v-model="showPicker"
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
        v-if="showPicker"
        v-model="timeIso"
        @click:minute="$refs.menu.save(timeIso)"
      />
			-->
    </v-menu>
  </div>
  <span v-else>
    {{ formatTime(new Date(variable.value)) }}
  </span>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { currentTimeZone, formatTime } from '../../logic/utils'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'TimeVariableFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false,
    }
  },

  data: () => ({
    timeIso: '',
    showPicker: false,
  }),

  created() {
    this.timeIso = this.variable.value.slice(11, 16)
  },

  methods: {
    formatTime,

    provideNewValue(): string {
      return new Date().toISOString()
        .replace(/T\d\d:\d\d/, 'T' + this.timeIso)
        .slice(0, -1) + currentTimeZone()
    },
  },
})
</script>
