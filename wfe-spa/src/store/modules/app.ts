// Модуль хранит состояние приложения, например данные для списка пунктов меню
import { make } from 'vuex-pathify';

const state = {
  serverUrl: '',
  swagger: null,
  drawer: null,
  mini: false,
  items: [
    // {
    //   title: 'Рабочий стол',
    //   icon: 'mdi-view-dashboard',
    //   to: '/',
    // },
    {
      title: 'Мои задачи',
      icon: 'mdi-calendar-check',
      to: '/task/list/',
    },
    {
      title: 'Запустить процесс',
      icon: 'mdi-play-box',
      to: '/process/definition/list/',
    },
    {
      title: 'Запущенные процессы',
      icon: 'mdi-graph-outline',
      to: '/process/list/',
    },
    {
      title: 'Отчеты',
      icon: 'mdi-poll-box-outline',
      to: '/report/list/',
    },
  ],
  profile: [
    { 
      title: 'Профиль пользователя', 
      icon: 'mdi-account-cog', 
      to: '/profile' 
    },
    { 
      divider: true 
    },
    { 
      title: 'Выйти', 
      icon: 'mdi-location-exit', 
      to: '/logout' 
    },
  ],
};

const mutations = make.mutations(state);

const actions = {
  ...make.actions(state),
  init: async (context: any) => {
    if (process.env.NODE_ENV === 'development') {
      context.commit('serverUrl', 'http://localhost:8080');
    }
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
