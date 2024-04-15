interface Colors {
  primary: string
  secondary: string
  accent: string
  error: string
  info: string
  success: string
  warning: string
  'primary-background': string
  'primary-background-darken-1': string
  'primary-text': string
  'nav-bar': string
  'task-escalation': string
  'task-delegation': string // not used for now
  'task-substitution': string
}

export interface AppTheme {
  label: string
  logo: string
  navBarContrast: {
    light: string
    dark: string
  }
  dark: Colors
  light: Colors
}
