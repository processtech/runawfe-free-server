import { defineStore } from 'pinia'

export interface SystemState {
  serverUrl: string
  swaggerClient: any // TODO type
  publicPath: string
}

export const useSystemStore = defineStore('system', {
  state: (): SystemState => ({
    serverUrl: process.env.NODE_ENV === 'development'
      ? 'http://localhost:8080'
      : window.location.origin,
    swaggerClient: null,
    publicPath: process.env.NODE_ENV === 'development' ? '' : '/spa',
  }),

  actions: {
    setSwaggerClient(swaggerClient: any /* TODO type? */) {
      this.swaggerClient = this.swaggerClient
    },
  },
})
