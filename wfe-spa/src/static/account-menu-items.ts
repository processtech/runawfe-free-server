import type { MenuItem } from '../ts/menu-item'

export const accountMenuItems: Readonly<MenuItem[]> = Object.freeze([
  {
    title: 'Профиль пользователя',
    icon: 'mdi-account-cog',
    to: '/profile'
  },
  {
    title: 'Выйти',
    icon: 'mdi-location-exit',
    to: '/logout'
  },
])
