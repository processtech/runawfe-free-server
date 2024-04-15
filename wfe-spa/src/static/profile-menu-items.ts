import type { MenuItem } from '@/ts/menu-item'

export const profileMenuItems: Readonly<MenuItem[]> = Object.freeze([
  {
    title: 'Информация пользователя',
    icon: 'mdi-account-details-outline',
    to: 'account-details',
  },
  {
    title: 'Изменение пароля',
    icon: 'mdi-lock',
    to: 'change-password',
  },
  {
    title: 'Возможные заместители',
    icon: 'mdi-account-switch-outline',
    to: 'substitutions',
  },
  {
    title: 'Можете замещать',
    icon: 'mdi-account-star',
    to: 'may-substitute',
  },
  {
    title: 'Ваши группы',
    icon: 'mdi-account-group',
    to: 'groups',
  },
  {
    title: 'Список отношений',
    icon: 'mdi-state-machine',
    to: 'relations',
  },
  {
    title: 'Настройки интерфейса',
    icon: 'mdi-application-settings-outline',
    to: 'ui-preferences',
  },
//   {
//     title: 'Административный интерфейс',
//     icon: 'mdi-montor-shimmer',
//     to: 'admin-ui',
//   }
])
