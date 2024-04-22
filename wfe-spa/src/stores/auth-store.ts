import SwaggerClient from 'swagger-client'
import { defineStore } from 'pinia'
import { useSystemStore } from '@/stores/system-store'
import type { WfeUser } from '@/domain/wfe-user'
import { systemConfiguration } from '@/logic/system-configuration'

interface AuthState {
  token: string
  currentUser: WfeUser
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: JSON.parse(localStorage.getItem('runawfe@user') || '{}').token,
    currentUser: JSON.parse(localStorage.getItem('auth.currentUser') || '{}')
  }),

  actions: {
    update(token: string) {
      this.token = token
      localStorage.setItem('runawfe@user', JSON.stringify({ token }));
    },

    authenticate() {
      return new Promise((resolve, reject) => {
          const token = this.token;
          const systemStore = useSystemStore()
          const client = systemStore.swaggerClient
          if (!token) {
              reject(null);
          } else if (!client) {
              this.makeSwaggerClient({ token, resolve, reject });
          } else {
            this.validateToken({ token, client, resolve, reject });
          }
      });
    },

    makeSwaggerClient(params: any) {
        const { token, resolve, reject } = params;
        new SwaggerClient({
          url: systemConfiguration.serverUrl() + '/restapi/v3/api-docs',
          authorizations: {
            token: {
              value: token,
            },
          },
        }).then((client: any) => {
            this.validateToken({ token, client: client.apis, resolve, reject });
        }).catch((error: any) => {
            console.error(error);
        });
    },

    validateToken(params: any) {
        const { token, client, resolve, reject } = params;
        const systemStore = useSystemStore()
        client['auth-controller'].validateUsingPOST({ token })
          .then((data: any) => {
            systemStore.setSwaggerClient(client)
            resolve(client);
        }, (reason: string) => {
          reject(reason);
        }).catch((error: any) => {
          console.log(error);
        });
    },

    login(params: any) {
      return new Promise((resolve, reject) => {
        const { username: login, password } = params;
        const systemStore = useSystemStore()
        new SwaggerClient({ url: systemConfiguration.serverUrl() + '/restapi/v3/api-docs' })
          .then((client: any) => {
            client.apis['auth-controller'].basicUsingPOST(null, {
              requestBody: {
                login: login,
                password: password
              }})
            .then(
              (data: any) => {
                const token = data.body;
                this.update(token)
                this.makeSwaggerClient({ token, resolve, reject });
              },
              () => reject('Неверный логин или пароль!')
            ).catch(console.log);
          });
      });
    },

    saveUser(user: WfeUser): void {
      this.currentUser = user
      localStorage.setItem('auth.currentUser', JSON.stringify(user))
    },

    logout() {
      const systemStore = useSystemStore()
      systemStore.setSwaggerClient(null)
      this.update('')
      localStorage.removeItem('auth.currentUser')
      // TODO should clean currentUser?
    }
  }
})
