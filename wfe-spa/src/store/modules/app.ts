// Модуль хранит состояние приложения, например данные для списка пунктов меню
import { make } from 'vuex-pathify';

const state = {
  drawer: null,
  mini: false,
  items: [
    {
      title: 'Рабочий стол',
      icon: 'mdi-view-dashboard',
      to: '/',
    },
    {
      title: 'Мои задачи',
      icon: 'mdi-calendar-check',
      to: '/task/list/',
    },
    {
      title: 'Запустить процесс',
      icon: 'mdi-play-box',
      to: '/process/start/',
    },
    {
      title: 'Запущенные процессы',
      icon: 'mdi-graph-outline',
      to: '/process/list/',
    },
  ],
};

const mutations = make.mutations(state);

const actions = {
  ...make.actions(state),
  init: async (dispatch: any) => {
    //
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
