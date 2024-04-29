<template>
  <wfe-table>
    <v-data-table-server
      class="bg-primary-background"
      :headers="filteredHeaders"
      :items="items"
      :header-props="{ class: 'text-primary-text' }"
      :row-props="rowProps"
      item-key="id"
      :sort-by="options.sortBy"
      :items-per-page="options.itemsPerPage"
      :items-per-page-options="[ 10, 20, 50, 100 ]"
      :items-per-page-text="'Строк на странице'"
      :items-length="total"
      :loading="loading"
      @update:options="updateOptions"
      hover
      @click:row="(event: MouseEvent, row: any) => $router.push(`/process/${row.item.id}/card/`)"
      show-current-page
    >
      <template v-slot:top>
        <table-toolbar @reload="update">
          <template v-slot:filterControl>
            <filter-control-btn @toggleFilter="showFilters = !showFilters" />
          </template>
          <template v-slot:columnsControl>
            <table-columns-control :headers="headers">
              <template v-slot:variables>
                <variable-columns-control :processIds="items.map(i => i.id)" />
              </template>
            </table-columns-control>
          </template>
          <template v-slot:colorsControl>
            <colors-description-control :colors="colors" />
          </template>
        </table-toolbar>
      </template>
      <template v-slot:[`item.id`]="{ item }">
        <span
          @click.stop="$router.push(`/process/${item.id}/card`)"
          class="cursor-pointer text-decoration-underline"
        >
          {{ item.id }}
        </span>
      </template>
      <template v-slot:[`item.definitionName`]="{ item }">
        <span
          @click.stop="$router.push(`/process/definition/${item.definitionId}/card`)"
          class="cursor-pointer text-decoration-underline"
        >
          {{ item.definitionName }}
        </span>
      </template>
      <template v-slot:[`item.startDate`]="{ item }">
        {{ formatDateTime(new Date(item.startDate)) }}
      </template>
      <template v-slot:[`item.endDate`]="{ item }">
        {{ item.endDate ? formatDateTime(new Date(item.startDate)) : '' }}
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
      <template v-for="(variable, i) in variables" v-slot:[`item.${variable}`]="{ item }">
        <variable-cell :variable="item.variables.find(v => v.name === variable)" :key="i" />
      </template>
    </v-data-table-server>
  </wfe-table>
</template>

<script lang="ts">
import { createWfeTableOptions } from '../logic/wfe-table-component-options-factory'
import { processHeaders } from '../static/process-headers'
import { processService } from '../services/process-service'
import { type WfeProcess } from '../ts/WfeProcess'

export default createWfeTableOptions<WfeProcess>({
  name: 'ProcessList',
  headers: [...processHeaders],
  visibleColumns: ['id', 'definitionName', 'executionStatus', 'startDate', 'endDate'],
  colors: [
    {
      value: 'warning',
      description: 'Установленный срок окончания процесса подходит к концу'
    },
    {
      value: 'error',
      description: 'Процесс не завершён в установленный срок'
    }
  ],
  itemClassFunc: (process: WfeProcess): string => {
    const timestamp = new Date().getTime()
    // TODO add a class when process deadline is over
    if (new Date(process.endDate).getTime() < timestamp) {
      return 'bg-error'
    } else {
      return ''
    }
  },
}, processService.getProcesses)
</script>
