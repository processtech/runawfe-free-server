import type { PageList } from '@/ts/page-list'
import type { PagedListFilter } from '@/ts/paged-list-filter'
import { apiClient } from '@/logic/api-client'
import { WfeReport } from '@/ts/WfeReport'

export class ReportService {

  async getReports(requestBody: PagedListFilter): Promise<PageList<WfeReport>> {
    const client = await apiClient()
    const data = await client['report-controller'].getReportsUsingPOST(null, { requestBody })
    return data.body
  }
}

export const reportService = new ReportService()
