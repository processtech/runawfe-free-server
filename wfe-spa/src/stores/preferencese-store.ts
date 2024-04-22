import { defineStore } from 'pinia'
import { useMainMenuStore } from '@/stores/main-menu-store'

export interface PreferencesState {
  processStartFormByRowClick: boolean,
  editVariables: boolean,
  showChat: boolean,
}

export const usePreferencesStore = defineStore('preferences', {
  state: (): PreferencesState => ({
    processStartFormByRowClick: localStorage.getItem('preferences.processStartFormByRowClick')
      ? localStorage.getItem('preferences.processStartFormByRowClick') === 'true'
      : true,
    editVariables: localStorage.getItem('preferences.editVariables')
      ? localStorage.getItem('preferences.editVariables') === 'true'
      : false,
    showChat: localStorage.getItem('preferences.showChat')
      ? localStorage.getItem('preferences.showChat') === 'true'
      : true,
  }),

  actions: {
    toggleStartFormByRowClick(): void {
      this.processStartFormByRowClick = !this.processStartFormByRowClick
      localStorage.setItem(
        'preferences.processStartFormByRowClick',
        this.processStartFormByRowClick.toString()
      )
    },

    toggleEditVariables(): void {
      this.editVariables = !this.editVariables
      localStorage.setItem('preferences.editVariables', this.editVariables.toString())
    },

    toggleChat(): void {
      const mainMenuStore = useMainMenuStore()
      if (this.showChat) {
        mainMenuStore.hideItem('chats')
      } else {
        mainMenuStore.showItem('chats')
      }
      this.showChat = !this.showChat
      localStorage.setItem('preferences.showChat', this.showChat.toString())
    },
  },
})
