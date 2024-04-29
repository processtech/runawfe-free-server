import type { TableHeader } from '@/ts/table-header'

export const taskHeaders: Readonly<TableHeader []> = Object.freeze([
  {
      title: 'Задача',
      align: 'start',
      value: 'name',
      width: '20em',
      format: 'string',
      filterable: true,
      sortable: true,
  },
  {
      title: 'Описание',
      value: 'description',
      width: '20em',
      format: 'string',
      filterable: true,
  },
  {
      title: 'Номер экземпляра',
      value: 'processId',
      width: '8em',
      format: 'long',
      filterable: true,
      sortable: true,
  },
  {
      title: 'Процесс',
      value: 'definitionName',
      width: '20em',
      format: 'string',
      filterable: true,
      sortable: true,
  },
  {
      title: 'Создана',
      value: 'createDate',
      width: '12em',
      format: 'date-time',
      filterable: true,
      sortable: true,
  },
  {
      title: 'Время окончания',
      value: 'deadlineDate',
      width: '12em',
      format: 'date-time',
      filterable: true,
      sortable: true,
  },
])
