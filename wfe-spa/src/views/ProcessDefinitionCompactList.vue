<template>
  <wfe-table>
    <v-data-table-server
      class="bg-primary-background"
      :headers="filteredHeaders"
      :header-props="{ class: 'text-primary-text' }"
      :items="items"
      item-key="id"
      :items-per-page="options.itemsPerPage"
      :row-props="rowProps"
      :sort-by="options.sortBy"
      :group-by="options.groupBy"
      :items-per-page-options="[ 10, 20, 50, 100 ]"
      :items-per-page-text="'Строк на странице'"
      :items-length="total"
      :loading="loading"
      loading-text="Загрузка данных..."
      :hide-default-header="true"
      @update:options="updateOptions"
      hover
      show-current-page
    >
      <template #top>
        <table-toolbar @reload="update">
          <template v-slot:filterControl>
            <filter-control-btn @toggleFilter="showFilters = !showFilters" />
          </template>
          <template #columnsControl>
            <v-btn
              icon="mdi-ballot-outline"
              @click="toggleProcessDefinitionShowCategory"
              :variant="processDefinitionShowCategory ? 'tonal' : 'text'"
            />
          </template>
        </table-toolbar>
      </template>
      <template #group-header="{ item, columns, toggleGroup, isGroupOpen }">
       <tr>
          <td :colspan="columns.length">
            <v-btn
              :icon="isGroupOpen(item) ? '$expand' : '$next'"
              size="small"
              variant="text"
              @click="toggleGroup(item)"
            ></v-btn>
            {{ item.value }}
          </td>
        </tr>
      </template>
      <template #item.name="{ item }">
        <div class="py-2" @click="$emit('rowClick', item.id)">
          {{ item.name }}
        </div>
      </template>
      <template #no-data>
        {{ loadingError ? 'Ошибка загрузки данных' : 'Данные отсутствуют' }}
      </template>
      <template #body.prepend>
        <tr v-show="showFilters">
          <td class="py-1" colspan="2">
            <filter-cell
              :header="processDefinitionCategoryHeader"
              :key="processDefinitionCategoryHeader.value"
              :renderTd="false"
            />
            <filter-cell
              v-for="header in filteredHeaders"
              :header="header"
              :key="header.value"
              :renderTd="false"
            />
          </td>
        </tr>
      </template>
    </v-data-table-server>
  </wfe-table>
</template>

<script lang="ts">
import type { WfeProcessDefinition } from '@/ts/WfeProcessDefinition'
import { processDefinitionService } from '@/services/process-definition-service'
import { createWfeTableOptions } from '@/logic/wfe-table-component-options-factory'

export default createWfeTableOptions<WfeProcessDefinition>({
  name: 'ProcessDefinitionCompactList',
  // key: 'data-table-group' is placed here to avoid 'Group' column label
  // https://github.com/vuetifyjs/vuetify/blob/master/packages/vuetify/src/components/VDataTable/composables/headers.ts#L241
  headers: [
    {
      key: 'data-table-group',
      title: '',
      value: 'data-table-group',
      width: '1px',
      format: 'string',
      filterable: false,
    },
    {
      title: 'Название',
      value: 'name',
      width: '20em',
      format: 'string',
      filterable: true,
      sortable: true,
    },
  ],
  visibleColumns: ['data-table-group', 'name'],
  extraOptions: {
    data: () => ({
      processDefinitionCategoryHeader: {
        title: 'Категория',
        value: 'category',
        format: 'processDefinitionCategory',
        filterable: true,
      },
      processDefinitionShowCategory: false,
    }),

    created() {
      if (this.options.groupBy.length) {
        this.processDefinitionShowCategory = true
      }
    },

    methods: {
      toggleProcessDefinitionShowCategory() {
        this.processDefinitionShowCategory = !this.processDefinitionShowCategory
        if (this.processDefinitionShowCategory) {
          this.options.groupBy = [{'key':'category', 'order': 'asc'}]
        } else {
          this.options.groupBy = []
        }
        const componentName = this.$options.name
        this.saveOptions(componentName, this.options)
      },
    },
  },
}, processDefinitionService.getDefinitions)
</script>
