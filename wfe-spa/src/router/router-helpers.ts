import { kebabCase } from 'lodash';
import Index from '../layouts/default/Index.vue';
import Login from '../layouts/auth/Login.vue';

export function layout (layout = 'Default', children: any = [], path = '') {
  const dir = kebabCase(layout);
  let component: any = null;
  let name: string = '';
  if (dir === 'default') {
    name = 'Index';
    component = Index;
  } else if (dir === 'auth') {
    name = 'Login';
    component = Login;
    path = '/login';
  }
  return {
    children,
    component,
    path,
    name,
  }
}