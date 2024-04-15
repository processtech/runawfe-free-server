import { createVuetify } from 'vuetify'
import { themes } from '@/static/themes'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

function mapThemes(): Record<string, { dark: boolean, colors: { [key: string]: string } }> {
  return Object.entries(themes)
    .map(([name, theme]) => ({
      [`${name}-light`]: { dark: false, colors: theme.light },
      [`${name}-dark`]: { dark: true, colors: theme.dark },
    }))
    .reduce((t, acc) => {
      Object.keys(t).forEach(key => acc[key] = t[key])
      return acc
    }, {})
}

export const vuetify = createVuetify({
  theme: {
    themes: mapThemes()
  },
  components,
  directives,
})
