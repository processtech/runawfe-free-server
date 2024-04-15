import type { TableHeader } from '@/ts/table-header'

export const processDefinitionHeaders: Readonly<TableHeader[]> = Object.freeze([
  {
    title: 'Запустить',
    value: 'start',
    align: 'center',
    visible: true,
    sortable: false,
    width: '1px',
    filterable: false,
  },
  {
    title: 'Имя',
    align: 'start',
    value: 'name',
    visible: true,
    width: '20em',
    format: 'string',
    filterable: true,
  },
  {
    title: 'Описание',
    value:'description',
    visible: false,
    width: '20em',
    format: 'string',
    filterable: true,
  },
  {
    title: 'Дата загрузки',
    value: 'createDate',
    visible: true,
    width: '12em',
    format: 'date-time',
    filterable: true,
  },
  {
    title: 'Автор загрузки',
    value: 'createActor',
    visible: true,
    sortable: false,
    width: '12em',
    filterable: false,
  },
  {
    title: 'Дата обновления',
    value: 'updateDate',
    visible: false,
    width: '12em',
    format: 'date-time',
    filterable: true,
  },
  {
    title: 'Автор обновления',
    value: 'updateActor',
    visible: false,
    sortable: false,
    width: '12em',
    filterable: false,
  }
])
