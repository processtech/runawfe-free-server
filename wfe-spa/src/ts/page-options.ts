type SortItem = { key: string, order?: 'asc' | 'desc' }

export interface PageOptions {
  page: number
  itemsPerPage: number
  sortBy: SortItem[]
  groupBy: SortItem[]
}
