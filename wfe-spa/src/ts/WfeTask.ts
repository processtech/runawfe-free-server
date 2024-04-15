import { WfeExecutor } from './WfeExecutor';
import type { WfeVariable } from './WfeVariable';

export interface WfeTask {
  id: number
  name: string
  category: string
  nodeId: string
  description: string
  swimlaneName: string
  owner: WfeExecutor
  targetActor: WfeExecutor
  definitionVersionId: number
  definitionName: string
  processId: number
  processHierarchyIds: string
  tokenId: number
  createDate: Date
  deadlineDate: Date
  deadlineWarningDate: Date
  assignDate: Date
  escalated: boolean
  firstOpen: boolean
  acquiredBySubstitution: boolean
  multitaskIndex: number
  readOnly: boolean
  variables: Array<WfeVariable>
}
