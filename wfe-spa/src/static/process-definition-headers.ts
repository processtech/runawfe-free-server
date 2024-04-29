import type { TableHeader } from '@/ts/table-header'

export const processDefinitionHeaders: Readonly<TableHeader[]> = Object.freeze([
  {
    title: 'Запустить',
    value: 'start',
    align: 'center',
    width: '1px',
  },
  {
    title: 'Название',
    align: 'start',
    value: 'name',
    width: '30em',
    format: 'string',
    filterable: true,
    sortable: true,
  },
  {
    title: 'Тип',
    value: 'category',
    width: '20em',
    format: 'processDefinitionCategory',
    filterable: true,
  },
  {
    title: 'Описание',
    value: 'description',
    width: '30em',
    format: 'string',
    filterable: true,
    sortable: true,
  },
  {
    title: 'Дата загрузки',
    value: 'createDate',
    width: '30em',
    format: 'date-time',
    filterable: true,
    sortable: true,
  },
  {
    title: 'Загрузил',
    value: 'createUser',
    width: '30em',
  },
  {
    title: 'Дата обновления',
    value: 'updateDate',
    width: '30em',
    format: 'date-time',
    filterable: true,
    sortable: true,
  },
  {
    title: 'Обновил',
    value: 'updateUser',
    width: '30em',
  }
])
