import type { WfeVariable } from './WfeVariable';

export interface WfeProcess {
  id: number
  definitionName: string
  definitionId: number
  executionStatus: string
  startDate: Date
  endDate: Date
  variables: Array<WfeVariable>
}
