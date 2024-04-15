import { Base64 } from 'js-base64';

enum EmptyJsonType {
  Object = '{}',
  Array = '[]'
}

export class UrlEncoder {
  private cache: Map<string, any> = new Map()

  encode(object: Object): string {
    if (!Object.keys(object).length) {
      return ''
    }
    return Base64.encodeURI(JSON.stringify(object))
  }

  decode<T extends Object>(url: string): T {
    return this.doDecode(url, EmptyJsonType.Object)
  }

  decodeArray(url: string): any[] {
    return this.doDecode(url, EmptyJsonType.Array)
  }

  private doDecode<T extends Object>(url: string, type: EmptyJsonType): T {
    if (!url) {
      return JSON.parse(type)
    }
    if (this.cache.has(url)) {
      return this.cache.get(url)
    }
    const object = JSON.parse(Base64.decode(url))
    this.cache.set(url, object)
    return object
  }
}

export const urlEncoder = new UrlEncoder()
