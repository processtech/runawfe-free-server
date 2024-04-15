<template>
  <wfe-table>
    <v-data-table-server
      class="bg-primary-background primaryText--text"
      :headers="filteredHeaders"
      :header-props="{ class: 'text-primary-text' }"
      :items="items"
      :row-props="rowProps"
      item-key="id"
      :sort-by="options.sortBy"
      :items-per-page="options.itemsPerPage"
      :items-per-page-options="[ 10, 20, 50, 100 ]"
      :items-per-page-text="'Строк на странице'"
      :items-length="total"
      :loading="loading"
      @click:row="(e, row) => $emit('rowClick', row.item.id)"
      @update:options="updateOptions"
      hover
    >
      <template v-slot:top>
        <table-toolbar>
          <template v-slot:filterControl>
            <filter-control-btn @toggleFilter="showFilters = !showFilters" />
          </template>
          <template v-slot:columnsControl>
            <table-columns-control :headers="headers" />
          </template>
        </table-toolbar>
      </template>
      <template v-slot:[`item.start`]="{ item }">
        <v-icon
          color="accent"
          size="large"
          class="mr-2"
          :disabled="!item.canBeStarted"
          @click.stop="() =>$router.push(`/process/definition/${item.id}/card/`)"
        >
          mdi-play-circle
        </v-icon>
      </template>
      <template v-slot:[`item.createDate`]="{ item }">
        {{ formatDate(new Date(item.createDate)) }}
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
import { processDefinitionHeaders } from '../static/process-definition-headers'
import { WfeProcessDefinition } from '../ts/WfeProcessDefinition'
import { processDefinitionService } from '../services/process-definition-service'
import { createWfeTableOptions } from '../logic/wfe-table-component-options-factory'

export default createWfeTableOptions<WfeProcessDefinition>({
  name: 'ProcessDefinitionList',
  headers: [...processDefinitionHeaders],
  visibleColumns: ['start', 'name', 'createDate', 'createActor'],
}, processDefinitionService.getDefinitions)
</script>
