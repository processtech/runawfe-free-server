import { defineStore } from 'pinia'

export interface SystemState {
  swaggerClient: any // TODO type
}

export const useSystemStore = defineStore('system', {
  state: (): SystemState => ({
    swaggerClient: null,
  }),

  actions: {
    setSwaggerClient(swaggerClient: any /* TODO type? */) {
      this.swaggerClient = this.swaggerClient
    },
  },
})
