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
      :items-per-page-options="[ 10, 20, 50, 100 ]"
      :items-per-page-text="'Строк на странице'"
      :items-length="total"
      :loading="loading"
      :hide-default-header="true"
      @update:options="updateOptions"
      hover
      show-current-page
    >
      <template v-slot:top>
        <table-toolbar @reload="update">
          <template v-slot:filterControl>
            <filter-control-btn @toggleFilter="showFilters = !showFilters" />
          </template>
        </table-toolbar>
      </template>
      <template v-slot:[`item.name`]="{ item }">
        <div class="py-2" @click="$emit('rowClick', item.id)">
          {{ item.name }}
        </div>
      </template>
      <template v-slot:no-data>Данные отсутствуют</template>
      <template v-slot:[`body.prepend`]>
        <tr v-show="showFilters">
          <filter-cell v-for="header in filteredHeaders" :header="header" :key="header.value" />
        </tr>
      </template>
    </v-data-table-server>
  </wfe-table>
</template>

<script lang="ts">
import { WfeProcessDefinition } from '../ts/WfeProcessDefinition'
import { processDefinitionService } from '../services/process-definition-service'
import { createWfeTableOptions } from '../logic/wfe-table-component-options-factory'

export default createWfeTableOptions<WfeProcessDefinition>({
  name: 'ProcessDefinitionCompactList',
  headers: [
    {
      title: 'Имя',
      align: 'start',
      value: 'name',
      width: '20em',
      format: 'string',
      filterable: true,
      sortable: true,
    }
  ],
  visibleColumns: ['name'],
}, processDefinitionService.getDefinitions)
</script>
