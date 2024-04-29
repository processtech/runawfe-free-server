import { formatDate, formatDateTime } from '@/logic/utils'
import { defineComponent } from 'vue'
import { useUiStore } from '@/stores/ui-store'
import { mapActions, mapState } from 'pinia'
import type { PageOptions } from '@/ts/page-options'
import type { TableHeader } from '@/ts/table-header'
import type { ColorDescription } from '@/ts/color-description'
import type { PageList } from '@/ts/page-list'
import type { PagedListFilter } from '@/ts/paged-list-filter'
import FilterCell from '@/components/FilterCell.vue'
import TableToolbar from '@/components/TableToolbar.vue'
import WfeTable from '@/components/WfeTable.vue'
import FilterControlBtn from '@/components/FilterControlBtn.vue'
import TableColumnsControl from '@/components/TableColumnsControl.vue'
import ColorsDescriptionControl from '../components/ColorsDescriptionControl.vue'
import VariableCell from '@/components/VariableCell.vue'
import VariableColumnsControl from '@/components/VariableColumnsControl.vue'
import { wfeRouter } from '@/logic/wfe-router'
import type { ComponentOptionsMixin, DefineComponent, ExtractPropTypes, PublicProps } from 'vue'

declare type WfeTableParams<I> = {
  name: string
  headers: TableHeader[]
  visibleColumns: string[]
  colors?: ColorDescription[]
  itemClassFunc?: (item: I) => string
  extraOptions?: ComponentOptionsMixin
}

declare type WfeTableOptions<I> = DefineComponent<
  {},
  {},
  any, // TODO try do not to use any
  {
    filter: () => { [key: string]: string }
    variables: () => string[]
    visibleColumns: () => string[]
    headers(): TableHeader[]
    filteredHeaders(): TableHeader[]
    getOptionsByKey: () => (key: string) => PageOptions
  },
  {
    updateOptions(options: PageOptions): void
    initVisibleColumnsIfNotPresent(): void
    rowProps(row: { item: I; }): { class?: string | undefined }
    update(): void
    toggleMini: () => void
    saveOptions: (key: string, options: PageOptions) => void
    formatDate: (date: Date) => string
    formatDateTime: (date: Date) => string
  },
  ComponentOptionsMixin,
  ComponentOptionsMixin,
  {},
  string,
  PublicProps,
  Readonly<ExtractPropTypes<{}>>,
  {},
  {}
 >

export function createWfeTableOptions<I>(
  params: WfeTableParams<I>,
  serviceFunc: (body: PagedListFilter) => Promise<PageList<I>>
): WfeTableOptions<I> {
  return defineComponent({
    name: params.name,

    components: {
      FilterCell,
      TableToolbar,
      WfeTable,
      FilterControlBtn,
      TableColumnsControl,
      ColorsDescriptionControl,
      VariableCell,
      VariableColumnsControl,
    },

    data: () => {
      const extraData = (params.extraOptions && params.extraOptions.data)
        ? params.extraOptions.data({})
        : {}
      return {
        ...extraData,
        showFilters : false,
        options: {} as PageOptions,
        total: 0,
        items: [] as I[],
        loading: true,
        // TODO rm3526
        loadingError: false,
        colors: [] as ColorDescription[] | undefined,
      }
    },

    computed: {
      ...mapState(useUiStore, ['getOptionsByKey']),

      filter: (): { [key: string]: string } => wfeRouter.queryObject('filter'),
      variables: (): string[] => wfeRouter.queryArray('vars'),
      visibleColumns: (): string[] => wfeRouter.queryArray('visible'),

      headers(): TableHeader[] {
        const variableHeaders: TableHeader[] = this.variables
          .map((v: string) => ({
            title: v,
            value: v,
            width: '12rem',
            filterable: false,
            isVariable: true,
          }))
        return params.headers.concat(variableHeaders)
      },

      filteredHeaders(): TableHeader[] {
        return this.headers
          .filter((h: TableHeader) => this.visibleColumns.includes(h.value))
      },
    },

    watch: {
      '$route.query.filter': function() {
        this.update()
      },
      '$route.query.vars': function() {
        this.update()
      },
      '$route.query.visible': function deleteFilterIfColumnBeenHidden() {
        this.initVisibleColumnsIfNotPresent()
        const filter = { ...this.filter }
        for (const key in filter) {
          if (!this.visibleColumns.includes(key)) {
            delete filter[key]
            wfeRouter.mergeQueryParams({ filter })
            break
          }
        }
      }
    },

    created() {
      this.colors = params.colors
      this.options = this.getOptionsByKey(params.name)
      this.initVisibleColumnsIfNotPresent()
      if (params.extraOptions && params.extraOptions.created) {
        params.extraOptions.created.call(this)
      }
    },

    methods: {
      ...params.extraOptions?.methods,

      formatDate,
      formatDateTime,

      ...mapActions(useUiStore, ['saveOptions']),

      updateOptions(options: PageOptions): void {
        this.options = options
        this.saveOptions(params.name, options)
        this.update()
      },

      initVisibleColumnsIfNotPresent(): void {
        if (this.$route.query.visible === undefined) {
          wfeRouter.mergeQueryParams({ visible: params.visibleColumns })
        }
      },

      rowProps(row: { item: I }): { class?: string } {
        let cssClass = 'text-primary-text'
        if (params.itemClassFunc) {
          cssClass += ' ' + params.itemClassFunc(row.item)
        }
        return { class: cssClass }
      },

      update(): void {
        this.loading = true;
        this.loadingError = false
        const { page, itemsPerPage, sortBy } = this.options;
        const query = {
          filters: this.filter,
          pageNumber: page,
          pageSize: itemsPerPage,
          sortings: sortBy?.map(({key, order}: { key: string, order: string }) => {
            return { name: key, order }
          }),
          variables: this.variables,
        }
        serviceFunc(query).then(paged => {
          // @ts-ignore TODO fix
          this.items = paged.data
          this.total = paged.total
        }).catch((error: Error) => {
          console.log(error)
          this.items = []
          this.total = 0
          this.loadingError = true
        }).finally(() => {
          this.loading = false
        })
      },
    },
  })
 }
