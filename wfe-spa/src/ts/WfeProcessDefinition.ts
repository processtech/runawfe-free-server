import { WfeExecutor } from './WfeExecutor';

export interface WfeProcessDefinition {
  id: number
  name: string
  description: string
  categories: Array<string>
  version: number
  canBeStarted: boolean
  createDate: Date
  createActor: WfeExecutor
  updateDate: Date
  updateActor: WfeExecutor
}
