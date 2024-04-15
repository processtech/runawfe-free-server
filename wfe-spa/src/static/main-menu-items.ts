import type { MenuItem } from '../ts/menu-item'

export const mainMenuItems : Readonly<MenuItem[]> = Object.freeze([
  {
    title: 'Мои задачи',
    icon: 'my-tasks',
    to: '/task/list/',
  },
  {
    title: 'Запустить процесс',
    icon: 'process-def',
    to: '/process/definition/list/',
  },
  {
    title: 'Запущенные процессы',
    icon: 'processes',
    to: '/process/list/'
  },
  {
    title: 'Отчеты',
    icon: 'reports',
    to: '/report/list/',
  },
])
