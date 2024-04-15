<template>
  <expanding-cell>
    <v-table class="my-1 bg-primary-background" density="compact">
      <tbody>
        <tr v-for="(item, index) in items" :key="index" class="list-value text-primary-text">
          <td class="text-body-2">
            <component
              :is="itemVariableContext.componentName()"
              :variable="{
                name: `${variable.name}-${index}`,
                format: itemFormat,
                value: item,
              }"
              :editing="editing"
              ref="items"
            />
          </td>
          <td v-if="editing" style="width: 40px">
            <v-chip close color="red" text-color="white" small @click="deleteItem(index)">
              Удалить
            </v-chip>
          </td>
        </tr>
      </tbody>
     </v-table>
     <v-btn v-if="editing" class="mx-3" color="primary" small icon dark @click="newItem">
       <v-icon>mdi-plus</v-icon>
     </v-btn>
  </expanding-cell>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { VariableContext } from '../../logic/variable-context'
import { extractContentFormat } from '../../logic/utils'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'ListVariableFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false,
    },
  },

  data: () => ({
    items: [],
    itemVariableContext: {} as VariableContext,
    itemFormat: '',
  }),

  created() {
    this.items = this.variable.value
    this.itemFormat = extractContentFormat(this.variable.format)
    this.itemVariableContext = new VariableContext(this.itemFormat)
  },

  methods: {
    newItem(): void {
      this.items.push(this.itemVariableContext.defaultValue())
    },

    deleteItem(index: number): void {
      this.items.splice(index, 1)
    },

    provideNewValue(): Array<any> {
      return this.$refs.items.map((i: any) => i.provideNewValue())
    },
  },
})
</script>

<style lang="scss">
  #variables {
    .list-value:hover {
      background-color: rgb(var(--v-theme-secondary));
      filter: brightness(90%);
    }
  }
</style>
