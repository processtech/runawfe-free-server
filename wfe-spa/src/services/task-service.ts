import type { PageList } from '@/ts/page-list'
import type { PagedListFilter } from '@/ts/paged-list-filter'
import { apiClient } from '@/logic/api-client'
import type { WfeTask } from '@/ts/WfeTask'

export class TaskService {

  async getTasks(requestBody: PagedListFilter): Promise<PageList<WfeTask>> {
    const client = await apiClient()
    const data = await client['task-controller'].getMyTasksUsingPOST(null, { requestBody })
    return data.body
  }

  async getTask(id: number): Promise<WfeTask> {
    const client = await apiClient()
    const data = await client['task-controller'].getTaskUsingGET({ id })
    return data.body
  }
}

export const taskService = new TaskService()
