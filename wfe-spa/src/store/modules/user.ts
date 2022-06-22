// Модуль хранит состояние приложения для пользователя. 
// В LocalStorage записываются такие настройки как выбранная тема оформления, оповещения и др.
import { make } from 'vuex-pathify';
import SwaggerClient from 'swagger-client';
import app from './app';

const state = {
    token: '',
    dark: true, // TODO Сделать через геттеры возможность переключения темы
    notifications: Array,
};

const mutations = make.mutations(state);

const actions = {
    fetch: (context: any) => {
        const local = localStorage.getItem('runawfe@user') || '{}';
        const user = JSON.parse(local);
    
        for (const key in user) {
            context.commit(key, user[key]);
        }
    },
    update: (context: any) => {
        localStorage.setItem('runawfe@user', JSON.stringify(context.state));
    },
    authenticate: (context: any) => {
        return new Promise((resolve, reject) => {
            const token = context.state.token;
            const client = context.rootGetters['app/swagger'];
            if (!!!token) {
                reject(null);
            } else if (!client) {
                context.dispatch('makeSwaggerClient', { token, resolve, reject });
            } else {
                context.dispatch('validateToken', { token, client, resolve, reject });
            }
        });
    },
    login: (context: any, params: any) => {
        return new Promise((resolve, reject) => {
            const { username: login, password } = params;
            new SwaggerClient({
                url: context.rootGetters['app/serverUrl'] + '/restapi/v3/api-docs',
            }).then((client: any) => {
                client.apis['auth-controller'].basicUsingPOST({
                    login,
                    password
                }).then((data: any) => {
                    const token = data.body;
                    context.dispatch('makeSwaggerClient', { token, resolve, reject });
                }, (reason: string) => reject('Неверный логин или пароль!')).catch((error: any) => {
                    console.log(error);
                });
            });
        });
    },
    makeSwaggerClient: (context: any, params: any) => {
        const { token, resolve, reject } = params;
        new SwaggerClient({ 
            url: context.rootGetters['app/serverUrl'] + '/restapi/v3/api-docs',
            authorizations: {
                token: {
                    value: token,
                },
            },
        }).then((client: any) => {
            context.dispatch('validateToken', { token, client: client.apis, resolve, reject });
        }).catch((error: any) => {
            console.log(error);
        });
    },
    validateToken: (context: any, params: any) => {
        const { token, client, resolve, reject } = params;
        client['auth-controller'].validateUsingPOST({
            token
        }).then((data: any) => {
            const token = data.body;
            context.commit('token', token);
            context.commit('app/swagger', client, { root: true });
            context.dispatch('update');
            resolve(client);
        }, (reason: string) => {
            reject(reason);
        }).catch((error: any) => {
            console.log(error);
        });
    },
    logout: (context: any) => {
        context.commit('token', null);
        context.commit('app/swagger', null, { root: true });
        context.dispatch('update');
    }
};

const getters = {
    ...make.getters(state),
};

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters,
};
