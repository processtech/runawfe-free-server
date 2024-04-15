import type { WfeVariable } from './WfeVariable';

export interface WfeProcess {
  id: number
  definitionName: string
  executionStatus: string
  startDate: Date
  endDate: Date
  variables: Array<WfeVariable>
}
