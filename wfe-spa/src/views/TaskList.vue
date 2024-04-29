<template>
  <wfe-table>
    <v-data-table-server
      class="bg-primary-background"
      :headers="filteredHeaders"
      :header-props="{ class: 'text-primary-text' }"
      :items="items"
      item-key="id"
      :row-props="rowProps"
      :items-per-page="options.itemsPerPage"
      :sort-by="options.sortBy"
      :items-per-page-options="[ 20, 50, 100, 500 ]"
      :items-per-page-text="'Строк на странице'"
      :items-length="total"
      :loading="loading"
      @update:options="updateOptions"
      hover
      show-current-page
      @click:row="(event: MouseEvent, row: any) => $router.push(`/task/${row.item.id}/card/`)"
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
      <template v-slot:[`item.name`]="{ item }">
        <span
          @click.stop="$router.push(`/task/${item.id}/card/`)"
          class="cursor-pointer text-decoration-underline"
        >
          {{ item.name }}
        </span>
      </template>
      <template v-slot:[`item.processId`]="{ item }">
        <span
          @click.stop="$router.push(`/process/${item.processId}/card`)"
          class="cursor-pointer text-decoration-underline"
        >
          {{ item.processId }}
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
      <template v-slot:[`item.createDate`]="{ item }">
        {{ formatDateTime(new Date(item.createDate)) }}
      </template>
      <template v-slot:[`item.deadlineDate`]="{ item }">
        {{ item.deadlineDate ? formatDateTime(new Date(item.deadlineDate)) : '' }}
      </template>
      <template v-slot:no-data>Данные отсутствуют</template>
      <template v-slot:[`body.prepend`]>
        <tr v-if="showFilters">
          <filter-cell v-for="header in filteredHeaders" :header="header" :key="header.value" />
        </tr>
      </template>
      <template v-for="(variable, i) in variables" v-slot:[`item.${variable}`]="{ item }">
        <variable-cell :variable="item.variables.find(v => v.name === variable)" :key="i"/>
      </template>
    </v-data-table-server>
  </wfe-table>
</template>

<script lang="ts">
import { taskService } from '../services/task-service'
import type { WfeTask } from '../ts/WfeTask'
import { taskHeaders } from '../static/task-headers'
import { createWfeTableOptions } from '../logic/wfe-table-component-options-factory'
import VariableColumnsControl from '../components/VariableColumnsControl.vue'

export default createWfeTableOptions<WfeTask>({
  name: 'TaskList',
  headers: [...taskHeaders],
  visibleColumns: ['name', 'processId', 'definitionName', 'createDate', 'deadlineDate'],
  colors: [
    {
      value: 'warning',
      description: 'Установленный срок задачи подходит к концу'
    },
    {
      value: 'error',
      description: 'Задача не выполнена в установленный срок'
    },
    {
      value: 'task-escalation',
      description: 'Задача получена по эскалации'
    },
    {
      value: 'task-substitution',
      description: 'Задача получена по замещению'
    }
  ],
  itemClassFunc: (task: WfeTask): string => {
    let cssClass = ''
    const timestamp = new Date().getTime();
    if (task.acquiredBySubstitution) {
      cssClass = 'task-substitution'
    } else if (task.escalated) {
      cssClass = 'task-escalation'
    } else if (new Date(task?.deadlineDate).getTime() < timestamp) {
      cssClass = 'task-expiration'
    } else if (new Date(task?.deadlineWarningDate).getTime() < timestamp) {
      cssClass = 'warning'
    }
    if (task.firstOpen) {
      cssClass += ' font-weight-bold'
    }
    return 'bg-' + cssClass
  }
}, taskService.getTasks)
</script>
