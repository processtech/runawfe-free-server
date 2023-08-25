// Модуль хранит состояние приложения, например данные для списка пунктов меню
import { Options } from '../../ts/Options';
import { make } from 'vuex-pathify';
import Constants from '../../ts/Constants';

const state = {
  serverUrl: '',
  swagger: null,
  drawer: null,
  mini: localStorage.getItem('mini') === 'true',
  items: [
    // {
    //   title: 'Рабочий стол',
    //   icon: 'mdi-view-dashboard',
    //   to: '/',
    // },
    {
      title: 'Мои задачи',
      icon: 'mdi-calendar-check',
      to: Constants.TASKS_PATH,
      options: localStorage.getItem(Constants.TASKS_OPTIONS) !== null ? JSON.parse(localStorage.getItem(Constants.TASKS_OPTIONS) as string) : new Options()
    },
    {
      title: 'Запустить процесс',
      icon: 'mdi-play-box',
      to: Constants.DEFINITIONS_PATH,
      options: localStorage.getItem(Constants.DEFINITIONS_OPTIONS) !== null ? JSON.parse(localStorage.getItem(Constants.DEFINITIONS_OPTIONS) as string) : new Options()
    },
    {
      title: 'Запущенные процессы',
      icon: 'mdi-graph-outline',
      to: Constants.PROCESSES_PATH,
      options: localStorage.getItem(Constants.PROCESSES_OPTIONS) !== null ? JSON.parse(localStorage.getItem(Constants.PROCESSES_OPTIONS) as string) : new Options()
    },
    {
      title: 'Отчеты',
      icon: 'mdi-poll-box-outline',
      to: Constants.REPORTS_PATH,
      options: localStorage.getItem(Constants.REPORTS_OPTIONS) !== null ? JSON.parse(localStorage.getItem(Constants.REPORTS_OPTIONS) as string) : new Options()
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
  ]
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
