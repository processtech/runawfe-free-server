import { defineStore } from 'pinia'

export interface PreferencesState {
  processStartFormByRowClick: boolean,
  editVariables: boolean,
}

export const usePreferencesStore = defineStore('preferences', {
  state: (): PreferencesState => ({
    processStartFormByRowClick: localStorage.getItem('preferences.processStartFormByRowClick')
      ? localStorage.getItem('preferences.processStartFormByRowClick') === 'true'
      : true,
    editVariables : localStorage.getItem('preferences.editVariables')
      ? localStorage.getItem('preferences.editVariables') === 'true'
      : false
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
  },
})
