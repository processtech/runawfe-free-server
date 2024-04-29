type Option = {
  title: string
  value: string
}

export type TableHeader = {
  title: string
  value: string
  width: string
  key?: string
  format?: string
  sortable?: boolean
  filterable: boolean
//  align?: string TODO gives a type error but seems to work in vuetify ¯\_(ツ)_/¯
  options?: Option[]
  isVariable?: boolean
}
