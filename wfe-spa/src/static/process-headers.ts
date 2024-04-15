import type { TableHeader } from '@/ts/table-header'

export const processHeaders: Readonly<TableHeader[]> =  Object.freeze([
  {
    title: 'Номер экземпляра',
    align: 'start',
    value: 'id',
    width: '8em',
    format: 'long',
    filterable: true,
    sortable: true,
  },
  {
    title: 'Процесс',
    value: 'definitionName',
    width: '15em',
    format: 'string',
    link: true,
    filterable: true,
    sortable: true,
  },
  {
    title: 'Статус',
    value: 'executionStatus',
    width: '15em',
    format: 'options',
    options: [
      {
        title: 'Активен',
        value: 'ACTIVE',
      },
      {
        title: 'Завершен',
        value: 'ENDED',
      },
      {
        title: 'Приостановлен',
        value: 'SUSPENDED',
      },
      {
        title: 'Имеет ошибки выполнения',
        value: 'FAILED',
      },
    ],
    filterable: true,
    sortable: true,
  },
  {
    title: 'Запущен',
    value: 'startDate',
    width: '12em',
    format: 'date-time',
    filterable: true,
    sortable: true,
  },
  {
    title: 'Окончен',
    value: 'endDate',
    width: '12em',
    format: 'date-time',
    filterable: true,
    sortable: true,
  },
])
