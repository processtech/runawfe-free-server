<template>
  <wfe-table>
    <v-data-table-server
      class="bg-primary-background"
      :headers="filteredHeaders"
      :header-props="{ class: 'text-primary-text' }"
      :items="items"
      item-key="id"
      :item-class="rowProps"
      :items-per-page="options.itemsPerPage"
      :sort-by="options.sortBy"
      :items-per-page-options="[ 20, 50, 100, 500 ]"
      :items-per-page-text="'Строк на странице'"
      :items-length="total"
      :loading="loading"
      @click:row="(_event: MouseEvent, row: any) => {
        $router.push({ path: `/report/${row.item.id}/card` })
      }"
      @update:options="updateOptions"
      hover
      show-current-page
    >
      <template v-slot:top>
        <table-toolbar @reload="update">
          <template v-slot:filterControl>
            <filter-control-btn @toggleFilter="showFilters = !showFilters" />
          </template>
          <template v-slot:columnsControl>
            <table-columns-control :headers="headers" />
          </template>
        </table-toolbar>
      </template>
      <template v-slot:[`item.name`]="{ item }">
        <a
          @click.stop="() => $router.push(`/report/${item.id}/card/`)"
          class="text--primary text-decoration-underline"
        >
          {{ item.name }}
        </a>
      </template>
      <template v-slot:no-data>Данные отсутствуют</template>
      <template v-slot:[`body.prepend`]>
        <tr v-if="showFilters">
          <filter-cell v-for="header in filteredHeaders"
            :header="header"
            :key="header.value"
          />
        </tr>
      </template>
    </v-data-table-server>
  </wfe-table>
</template>

<script lang="ts">
import { createWfeTableOptions } from '../logic/wfe-table-component-options-factory'
import { reportService } from '../services/report-service'
import { WfeReport } from '../ts/WfeReport'

export default createWfeTableOptions<WfeReport>({
  name: 'ReportList',
  headers: [
    {
      title: 'Название',
      value: 'name',
      width: '20em',
      format: 'string',
      filterable: true,
    },
    {
      title: 'Описание',
      value:'description',
      width: '20em',
      format: 'string',
      filterable: true,
    },
    {
      title: 'Тип',
      value: 'category',
      width: '12em',
      format: 'string',
      filterable: true,
    }
  ],
  visibleColumns: ['name', 'category'],
}, reportService.getReports)
</script>
