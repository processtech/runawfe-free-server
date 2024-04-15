import { defineStore } from 'pinia'
import { type PageOptions } from '../ts/page-options'
import {defaultItemsPerPage} from '@/static/default-items-per-page'

function initOptions(): { [key: string]: PageOptions } {
  const options: { [key: string]: PageOptions } = {}
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key?.startsWith('ui.options')) {
      options[key] = JSON.parse(localStorage.getItem(key) || '{}')
    }
  }
  return options
}

export interface UiState {
  mini: boolean
  options: { [key: string]: PageOptions | undefined },
}

export const useUiStore = defineStore('ui', {
  state: (): UiState => ({
    mini: localStorage.getItem('ui.mini') === 'true',
    options: initOptions(),
  }),

  getters: {
    getOptionsByKey(): (key: string) => PageOptions {
      return key => {
        const options = this.options['ui.options.' + key]
        if (options !== undefined) {
          return options
        }
        return {
          page: 1,
          itemsPerPage: defaultItemsPerPage[key] || 10,
          sortBy: [],
          sortDesc: [],
          groupBy: [],
          groupDesc: [],
          multiSort: false,
          mustSort: false,
        }
      }
    },
  },

  actions: {
    toggleMini() {
      this.mini = !this.mini
      localStorage.setItem('ui.mini', this.mini.toString())
    },

    saveOptions(key: string, options: PageOptions) {
      this.options['ui.options.' + key] = options
      localStorage.setItem('ui.options.' + key, JSON.stringify(options))
    }
  },
})
