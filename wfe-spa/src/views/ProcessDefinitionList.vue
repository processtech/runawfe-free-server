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
      @click:row="(_e: MouseEvent, row: any) => $emit('rowClick', row.item.id)"
      @update:options="updateOptions"
      hover
    >
      <template #top>
        <table-toolbar @reload="update">
          <template v-slot:filterControl>
            <filter-control-btn @toggleFilter="showFilters = !showFilters" />
          </template>
          <template v-slot:columnsControl>
            <table-columns-control :headers="headers" />
          </template>
        </table-toolbar>
      </template>
      <template #item.start="{ item }">
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
      <template #item.createDate="{ item }">
        {{ formatDateTime(new Date(item.createDate)) }}
      </template>
      <template #item.createUser="{ item }">
        {{ item.createUser ? item.createUser.fullName : '' }}
      </template>
      <template #item.updateDate="{ item }">
        {{ item.updateDate ? formatDateTime(new Date(item.updateDate)) : '' }}
      </template>
      <template #item.updateUser="{ item }">
        {{ item.updateUser ? item.updateUser.fullName : '' }}
      </template>
      <template #no-data>
        {{ loadingError ? 'Ошибка загрузки данных' : 'Данные отсутствуют' }}
      </template>
      <template #body.prepend>
        <tr v-show="showFilters">
          <filter-cell v-for="header in filteredHeaders" :header="header" :key="header.value" />
        </tr>
      </template>
    </v-data-table-server>
  </wfe-table>
</template>

<script lang="ts">
import { processDefinitionHeaders } from '@/static/process-definition-headers'
import type { WfeProcessDefinition } from '@/ts/WfeProcessDefinition'
import { processDefinitionService } from '@/services/process-definition-service'
import { createWfeTableOptions } from '@/logic/wfe-table-component-options-factory'

export default createWfeTableOptions<WfeProcessDefinition>({
  name: 'ProcessDefinitionList',
  headers: [...processDefinitionHeaders],
  visibleColumns: ['start', 'name', 'category', 'createDate'],
}, processDefinitionService.getDefinitions)
</script>
