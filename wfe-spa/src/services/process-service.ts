import type { PageList } from '@/ts/page-list'
import type { PagedListFilter } from '@/ts/paged-list-filter'
import { apiClient } from '@/logic/api-client'
import type { WfeProcess } from '@/ts/WfeProcess'
import type { WfeVariable } from '@/ts/WfeVariable'

export class ProcessService {

  async getProcess(id: number): Promise<WfeProcess> {
    const client = await apiClient()
    const data = await client['process-controller'].getProcessUsingGET({ id })
    return data.body
  }

  async getProcesses(requestBody: PagedListFilter): Promise<PageList<WfeProcess>> {
    const client = await apiClient()
    const data = await client['process-controller'].getProcessesUsingPOST(null, { requestBody })
    return data.body
  }

  async getProcessGraph(id: number): Promise<string> {
    const client = await apiClient();
    const response = await client['process-controller'].getProcessGraphUsingGET({ id })
    return window.URL
      ? window.URL.createObjectURL(response.data)
      : window.webkitURL.createObjectURL(response.data)
  }

  async getProcessVariables(id: number): Promise<WfeVariable[]> {
    const client = await apiClient()
    const response = await client['process-controller'].getProcessVariablesUsingGET({ id })
    return this.filterVariablesByNotNull(response.body)
  }

  async getVariableNames(processIds: number[]): Promise<string[]> {
    const client = await apiClient()
    const response = await client['process-controller'].getVariableNamesUsingPOST(null, {
      requestBody: processIds
    })
    return response.body
  }

 // TODO response type
  async saveProcessVariable(id: number, variableName: string, newValue: any): Promise<any> {
    const client = await apiClient();
    return await client['process-controller'].updateProcessVariablesUsingPATCH({ id }, {
      requestBody: {
        [variableName]: newValue,
      },
    })
  }

  async getVariableFile(id: number, name: string): Promise<Blob> {
    const client = await apiClient();
    const response = await client['process-controller'].getFileVariableValueUsingGET({ id, name });
    return response.text;
  }

  private filterVariablesByNotNull(variables: WfeVariable[]): WfeVariable[] {
    return variables
      .filter(v => {
        if (v.type !== 'USER_TYPE') {
          return v.value !== undefined
        }
        return v.value.some((v: WfeVariable) => v.value !== undefined)
      })
      .map(v => {
        if (v.type !== 'USER_TYPE') {
          return v
        }
        return { ...v, value: this.filterVariablesByNotNull(v.value) }
      })
  }
}

export const processService = new ProcessService()
