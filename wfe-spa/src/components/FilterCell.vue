<template>
  <td v-if="header.filterable" class="py-1">
    <component :is="header.format + '-filter-format'"
      @updateInput="updateFilters"
      :initValue="filter[header.value]"
      :options="header.options"
      ref="cell"
    />
  </td>
  <td v-else></td>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue'
import type { TableHeader } from '../ts/table-header'
import { wfeRouter } from '../logic/wfe-router'

export default defineComponent({
  name: 'FilterCell',

  props: {
    header: {
      type: Object as PropType<TableHeader>,
      required: true,
    },
  },

  computed: {
    filter: (): { [key: string]: string } => wfeRouter.queryObject('filter')
  },

  watch: {
    '$route.query.filter': function(filter: string) {
      if (!filter) {
        // @ts-ignore TODO try to fix type error
        this.$refs.cell?.clear()
      }
    },
  },

  methods: {
    updateFilters(value: string) {
      const filter = {...this.filter, ...{ [this.header.value]: value }}
      if (!value) {
        delete filter[this.header.value]
      }
      wfeRouter.mergeQueryParams({ filter })
    },
  },
});
</script>
