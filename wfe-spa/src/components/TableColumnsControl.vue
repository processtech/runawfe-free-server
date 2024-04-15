<script setup lang="ts">
import { computed, type ComputedRef, ref, type Ref, type PropType } from 'vue'
import type { TableHeader } from '@/ts/table-header'
import { wfeRouter } from '@/logic/wfe-router'

const props = defineProps({
  headers: {
    type: Array as PropType<TableHeader[]>,
    required: true,
  },
})

const showDialog = ref(false)
const hovers: Ref<{ [label: string]: boolean }> = ref({})

const visibleColumns: ComputedRef<string[]> = computed(() => wfeRouter.queryArray('visible'))
const variables: ComputedRef<string[]> = computed(() => wfeRouter.queryArray('vars'))

function toggleColumn(value: boolean, name: string): void {
  const vc : Set<string> = new Set(visibleColumns.value)
  if (value) {
    vc.add(name)
  } else {
    vc.delete(name)
  }
  wfeRouter.mergeQueryParams({ visible: [ ...vc ] })
}

function deleteVariable(name: string): void {
  const vs = new Set(variables.value)
  vs.delete(name)
  wfeRouter.mergeQueryParams({ vars: [ ...vs ] })
}

function selectAll(): void {
  wfeRouter.mergeQueryParams({
    visible: props.headers.map(h => h.value)
  })
}

function unselectAll(): void {
  wfeRouter.mergeQueryParams({ visible: '' })
}
</script>

<template>
  <v-dialog v-model="showDialog" width="700" scroll-strategy="close">
    <template v-slot:activator="{ props }">
      <v-btn icon="mdi-view-grid-plus" :variant="showDialog ? 'tonal' : 'text'" v-bind="props" />
    </template>
    <v-card class="bg-primary-background">
      <v-card-title class="d-flex align-center">
        <h5>Настройка вида</h5>
        <v-spacer />
        <v-btn icon="mdi-close" flat class="bg-primary-background" @click="showDialog = false" />
      </v-card-title>
      <v-divider />
      <v-card-text class="py-0">
        <v-container>
          <v-row>
            <v-col cols="12" class="d-flex justify-space-around">
              <v-btn @click="selectAll" class="bg-primary-background" flat>Выбрать всё</v-btn>
              <v-btn @click="unselectAll" class="bg-primary-background" flat>Убрать всё</v-btn>
            </v-col>
          </v-row>
          <v-row>
            <slot name="variables" />
          </v-row>
          <v-row align="center">
            <v-col
              v-for="header in headers"
              :key="header.value"
              cols="12"
              sm="6"
              md="4"
              class="d-flex"
            >
              <v-checkbox
                class="mt-n3"
                color="primary"
                hide-details
                :value-comparator="() => visibleColumns.includes(header.value)"
                @update:modelValue="v => toggleColumn(Boolean(v), header.value)"
                @mouseenter="hovers[header.value] = true"
                @mouseleave="hovers[header.value] = false"
              >
                <template v-slot:label>
                  <span>{{ header.title }}</span>
                  <v-btn
                    v-if="header.isVariable"
                    class="mb-3 ml-2"
                    flat
                    :class="{ 'd-none': !hovers[header.value] }"
                    size="small"
                    @click="deleteVariable(header.title)"
                    icon="mdi-delete"
                  />
                </template>
              </v-checkbox>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>
