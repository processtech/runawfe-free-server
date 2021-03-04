export function leadingSlash (str: string) {
  return str.lastIndexOf('/', 0) === 0 ? str : '/' + str;
}

export function trailingSlash (str: string) {
  return str.indexOf('/', str.length - 1) ? str : str + '/';
}

export const wait = function (timeout: number) {
  return new Promise(resolve => setTimeout(resolve, timeout));
}
