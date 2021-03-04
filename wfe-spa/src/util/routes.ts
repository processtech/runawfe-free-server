import { kebabCase } from 'lodash';
import { leadingSlash, trailingSlash } from './helpers';
import Index from '../layouts/default/Index.vue';

export function layout (layout = 'Default', children: any, path = '') {
  const dir = kebabCase(layout);
  let component: any = null;
  if (dir === 'default') {
    component = Index;
  }
  return {
    children,
    component,
    path,
  }
}

export function redirect (
  path = '*',
  rhandler: any,
) {
  if (typeof path === 'function') {
    rhandler = path;
    path = '*';
  }

  return {
    path,
    redirect: (to: any) => {
      const rpath = rhandler(to);
      const url = rpath !== ''
        ? leadingSlash(trailingSlash(rpath))
        : rpath;

      return `/${url}`;
    },
  }
}
