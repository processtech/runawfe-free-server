import { ExecutorType } from '@/ts/ExecutorType'
import { WfeExecutor } from '@/ts/WfeExecutor'
import { apiClient } from '@/logic/api-client'

export class ExecutorService {

//  async getUserByName(name: string): Promise<WfeUser> {
//    const client = await apiClient();
//    const data = await client['executor-controller'].getUserUsingGET({ name });
//    return data.body;
//  }

  async getExecutorsByType(type: ExecutorType): Promise<WfeExecutor[]> {
    const client = await apiClient()
    const data = await client['executor-controller'].getExecutorsByTypeUsingGET({ type })
    return data.body.data
  }

  async isAdministrator(id: number): Promise<boolean> {
    const client = await apiClient()
    const data = await client['executor-controller'].isAdministratorUsingGET({ id })
    return data.body
  }
}

export const executorService = new ExecutorService()
