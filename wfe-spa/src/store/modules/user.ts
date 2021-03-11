// Модуль хранит состояние приложения для пользователя. 
// В LocalStorage записываются такие настройки как выбранная тема оформления, оповещения и др.
import { make } from 'vuex-pathify';

const state = {
    dark: true, // TODO Сделать через геттеры возможность переключения темы
    notifications: [],
};

const mutations = make.mutations(state);

const actions = {
    fetch: (args: any) => {
        const local = localStorage.getItem('vuetify@user') || '{}';
        const user = JSON.parse(local);
    
        for (const key in user) {
            args.commit(key, user[key]);
        }
    },
    update: (args: any) => {
        localStorage.setItem('vuetify@user', JSON.stringify(args.state));
    },
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
