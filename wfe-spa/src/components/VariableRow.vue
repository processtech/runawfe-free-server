<script setup lang="ts">
import { type WfeVariable } from '@/ts/WfeVariable'
import { ref, type PropType } from 'vue'
import { VariableContext } from '@/logic/variable-context'
import { processService } from '@/services/process-service'
import { storeToRefs } from 'pinia'
import { usePreferencesStore } from '@/stores/preferencese-store'
import { removeListElementFormatIfPresent } from '@/logic/utils'
import { useRoute } from 'vue-router'

const preferenceStore = usePreferencesStore()
const variableFormat = ref(null)
const route = useRoute()
const emit = defineEmits<{(e: 'saveVariable'): void}>()

const props = defineProps({
  variable: {
    type:  Object as PropType<WfeVariable>,
    required: true,
  },
  nested: {
    type: Boolean,
    default: false,
  },
})

const variableContext = ref(
  new VariableContext(removeListElementFormatIfPresent(props.variable.format))
)
const editing = ref(false)
const isHovering = ref(false)

const { editVariables } = storeToRefs(preferenceStore)

function save(): void {
  // @ts-ignore
  const newValue = variableFormat.value.provideNewValue()
  processService.saveProcessVariable(Number(route.params.id), props.variable.name, newValue)
    .then(() => emit('saveVariable'))
  cancelEditing()
}

function toggleEditing(): void {
  if (editVariables.value) {
    editing.value = true
  }
}

function cancelEditing(): void {
  setTimeout(() => editing.value = false, 200)
}
</script>

<template>
  <tr
    :class="{'bg-secondary': isHovering}"
    class="text-primary-text"
    @dblclick="toggleEditing"
    @mouseover.stop="isHovering = true"
    @mouseout="isHovering = false"
  >
    <td class="px-3 text-body-2">{{ variable.name }}</td>
    <td class="px-3 text-subtitle-2">{{ variable.format }}</td>
    <td class="px-0 text-body-2">
      <div class="px-1">
        <component
          :is="variableContext.componentName()"
          :editing="editing"
          :variable="variable"
          ref="variableFormat"
        />
      </div>
      <v-container v-if="!nested && editing">
        <v-row justify="start" style="margin: 0">
          <v-col md="3">
            <v-btn small @click="cancelEditing">Отмена</v-btn>
          </v-col>
          <v-col md="3">
            <v-btn color="primary" small @click="save">Сохранить</v-btn>
          </v-col>
        </v-row>
      </v-container>
    </td>
  </tr>
</template>
