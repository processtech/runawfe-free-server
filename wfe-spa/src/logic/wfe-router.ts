import { urlEncoder, type UrlEncoder } from '@/logic/url-encoder'
import router from '@/plugins/router'

class WfeRouter {
  constructor(private urlEncoder: UrlEncoder) {}

  mergeQueryParams(params: { [key: string]: string | Array<any> | Object }): void {
    const encoded = Object.entries(params)
      .map(([key, value]) => ({ [key]: this.urlEncoder.encode(value) }))
      .reduce((acc, param) => ({ ...acc, ...param }))
    router.push({
      query: {
        ...router.currentRoute.value.query,
        ...encoded,
      }
    })
  }

  queryArray(param: string): Array<any> {
    const value = router.currentRoute.value.query[param]
    return urlEncoder.decodeArray(value?.toString() || '')
  }

  queryObject<T extends Object>(param: string): T {
    const value = router.currentRoute.value.query[param]
    return urlEncoder.decode(value?.toString() || '')
  }
}

export const wfeRouter = new WfeRouter(urlEncoder)
