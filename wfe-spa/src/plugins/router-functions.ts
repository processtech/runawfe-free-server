import { kebabCase } from 'lodash';
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