import type { PageList } from '@/ts/page-list'
import type { PagedListFilter } from '@/ts/paged-list-filter'
import { apiClient } from '@/logic/api-client'
import type { WfeProcessDefinition } from '@/ts/WfeProcessDefinition'


export class ProcessDefinitionService {

  async getDefinitions(requestBody: PagedListFilter): Promise<PageList<WfeProcessDefinition>> {
    const client = await apiClient()
    const data = await client['definition-controller'].getProcessDefinitionsUsingPOST(
      null,
      { requestBody }
    )
    return data.body
  }

  async getDefinitionById(id: number): Promise<WfeProcessDefinition> {
    const client = await apiClient()
    const data = await client['definition-controller'].getProcessDefinitionByIdUsingGET({ id })
    return data.body
  }
}

export const processDefinitionService = new ProcessDefinitionService()
