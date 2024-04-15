import { themes } from '../static/themes'
import { vuetify } from '../plugins/vuetify'
import { defineStore } from 'pinia'

const DEFAULT_THEME = 'greenInvert'

export interface ThemeState {
  dark: boolean
  current: string
}

type ThemeContrast = 'dark' | 'light'

export const useThemeStore = defineStore('theme', {
  state: (): ThemeState => ({
    dark: localStorage.getItem('theme.dark') === 'true',
    current: localStorage.getItem('theme.current') || DEFAULT_THEME
  }),

  getters: {
    themes: (): { name: string, label: string }[] => {
      return Object.entries(themes)
        .map(([name, theme]) => ({
          name,
          label: theme.label,
        }))
    },

    themeContrast(): ThemeContrast {
      return this.dark ? 'dark' : 'light'
    },

    navBarContrast(): string  {
      return themes[this.current].navBarContrast[this.themeContrast]
    },

    logo(): string {
      return themes[this.current].logo
    },
  },

  actions: {
    init(): void {
      this.switchTo(this.current)
    },

    toggleContrast(): void {
      this.dark = !this.dark
      this.switchTo(this.current)
      localStorage.setItem('theme.dark', this.dark.toString())
    },

    switchTo(themeName: string): void {
      vuetify.theme.global.name.value = themeName + '-' + this.themeContrast
      this.current = themeName
      localStorage.setItem('theme.current', themeName)
    },
  },
})
