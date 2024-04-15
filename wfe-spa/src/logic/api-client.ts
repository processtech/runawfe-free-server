import router from '../plugins/router'
import { useSystemStore } from '../stores/system-store';
import { useAuthStore } from '../stores/auth-store';

export function apiClient(): any {
  const systemStore = useSystemStore()
  const authStore = useAuthStore()
  return new Promise((resolve, reject) => {
    const client = systemStore.swaggerClient
    const token = authStore.token
    reject = () => router.push({ name: 'Login' });
    if (!token) {
      reject(null);
    } else if (!client) {
      authStore.makeSwaggerClient({ token, resolve, reject });
    } else {
      authStore.validateToken({token, client, resolve, reject});
      // store.dispatch('user/getNewTasksCount'); // TODO request new tasks count here
    }
  });
}
