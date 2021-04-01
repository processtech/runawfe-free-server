// Модуль хранит состояние приложения для пользователя. 
// В LocalStorage записываются такие настройки как выбранная тема оформления, оповещения и др.
import { make } from 'vuex-pathify';
import SwaggerClient from 'swagger-client';

const state = {
    apiUrl: 'http://localhost:8080/restapi/v3/api-docs',
    token: '',
    dark: true, // TODO Сделать через геттеры возможность переключения темы
    notifications: [],
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
            if (!!context.state.token) {

                resolve('Пользователь авторизован');
            } else {
                reject('Пользователь не авторизован!');
            }
        });
    },
    login: (context: any, params: any) => {
        return new Promise((resolve, reject) => {
            const { username: login, password } = params;
            new SwaggerClient({
                url: context.getters.apiUrl,
            }).then((client: any) => {
                client.apis['auth-controller'].tokenUsingPOST({
                    login,
                    password
                }).then((data: any) => {
                    let token = data.body;
                    token = token.split(' ')[1];
                    context.dispatch('makeSwaggerClient', { token, resolve, reject });
                },
                (reason: string) => reject('Неверный логин или пароль!'));
            },
            (reason: string) => reject('Сервис не отвечает:' + reason));
        });
    },
    makeSwaggerClient: (context: any, params: any) => {
        const { token, resolve, reject } = params;
        new SwaggerClient({ 
            url: context.getters.apiUrl,
            authorizations: {
                token: {
                    value: token,
                },
            },
        }).then((client: any) => {
            context.commit('token', token);
            context.commit('app/swagger', client, { root: true });
            context.dispatch('update');
            if (resolve) {
                resolve(client);
            }
        },
        (reason: string) => {
            if (reject) {
                reject('Срок действия сессии истек. Попробуйте заново авторизоваться.');
            }
        });
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
