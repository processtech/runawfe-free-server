type Option = {
  title: string
  value: string
}

export interface TableHeader {
  title: string
  value: string
  width: string
  format?: string
  sortable?: boolean
  filterable: boolean
  align?: string
  options?: Option[]
  isVariable?: boolean
}
