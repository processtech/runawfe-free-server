export interface PagedListFilter {
  filters: { [key:string]: string }
  pageNumber: number
  pageSize: number
  sortings: { name: string, order: 'asc' | 'desc' | undefined }[]
  variables: string[]
}
