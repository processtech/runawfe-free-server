<template>
  <expanding-cell style="width: 100%">
    <v-table class="my-2 mx-3 rounded-0 bg-primary-background" density="compact">
      <thead>
        <tr>
          <th
            class="text-subtitle-2 text-medium-emphasis"
            width="150"
            style="min-width: 150px"
          >
            Имя
          </th>
          <th
            class="text-subtitle-2 text-medium-emphasis"
            width="100"
            style="min-width: 100px"
          >
            Формат
          </th>
          <th class="text-subtitle-2 text-medium-emphasis" style="min-width: 300px">Значение</th>
        </tr>
      </thead>
      <tbody>
        <variable-row
          v-for="variable in variable.value"
          :key="variable.name"
          :variable="variable"
          :nested="true"
        />
      </tbody>
    </v-table>
  </expanding-cell>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'UserTypeVariableFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false,
    },
  },
})
</script>
