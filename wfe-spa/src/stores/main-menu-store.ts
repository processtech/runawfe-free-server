import type { MenuItem } from '@/ts/menu-item'
import { defineStore } from 'pinia'

const staticItems: Readonly<MenuItem[]> = Object.freeze([
  {
    name: 'my-tasks',
    title: 'Мои задачи',
    icon: 'my-tasks',
    to: '/task/list/',
  },
  {
    name: 'start-process',
    title: 'Запустить процесс',
    icon: 'process-def',
    to: '/process/definition/list/',
  },
  {
    name: 'processes',
    title: 'Запущенные процессы',
    icon: 'processes',
    to: '/process/list/'
  },
  {
    name: 'reports',
    title: 'Отчеты',
    icon: 'reports',
    to: '/report/list/',
  },
  {
    name: 'chats',
    title: 'Чаты',
    icon: 'chat',
    to: '/chat/list/'
  },
])

interface MainMenuState {
  hiddenItemNames: Set<string>
  dynamicItems: MenuItem[]
}

export const useMainMenuStore = defineStore('mainMenu', {
  state: (): MainMenuState => ({
    hiddenItemNames: new Set(JSON.parse(localStorage.getItem('mainMenu.hiddenItemNames') || '[]')),
    dynamicItems: JSON.parse(localStorage.getItem('mainMenu.dynamicItems') || '[]'),
  }),

  getters: {
    menuItems(): MenuItem[] {
      return [ ...staticItems, ...this.dynamicItems ]
        .filter(item => !this.hiddenItemNames.has(item.name))
    },
  },

  actions: {
    addItem(item: MenuItem): void {
      this.dynamicItems.push(item)
      localStorage.setItem('mainMenu.dynamicItems', JSON.stringify(this.dynamicItems))
    },

    removeItem(name: string): void {
      if (this.dynamicItems.some(item => item.name === name)) {
        console.error(`menu item ${name} is not dynamic or does not exist`)
        return
      }
      this.dynamicItems = this.dynamicItems.filter(item => item.name !== name)
      localStorage.setItem('mainMenu.dynamicItems', JSON.stringify(this.dynamicItems))
    },

    hideItem(name: string): void {
      if (!this.menuItems.some(item => item.name === name)) {
        console.error(`item ${name} not found`)
        return
      }
      this.hiddenItemNames.add(name)
      localStorage.setItem('mainMenu.hiddenItemNames', JSON.stringify([...this.hiddenItemNames]))
    },

    showItem(name: string): void {
      const removed = this.hiddenItemNames.delete(name)
      if (!removed) {
        console.error(`item ${name} not found`)
        return
      }
      localStorage.setItem('mainMenu.hiddenItemNames', JSON.stringify([...this.hiddenItemNames]))
    },
  },
})
