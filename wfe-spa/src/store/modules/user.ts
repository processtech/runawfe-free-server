import { make } from 'vuex-pathify';
import IN_BROWSER from '../../util/globals';

const state = {
  dark: false,
  drawer: {
    image: 0,
    gradient: 0,
    mini: false,
  },
  gradients: [
    'rgba(0, 0, 0, .7), rgba(0, 0, 0, .7)',
    'rgba(228, 226, 226, 1), rgba(255, 255, 255, 0.7)',
    'rgba(244, 67, 54, .8), rgba(244, 67, 54, .8)',
  ],
  images: [
    require('../../images/sidebar-1.jpg'),
    require('../../images/sidebar-2.jpg'),
    require('../../images/sidebar-3.jpg'),
    require('../../images/sidebar-4.jpg'),
  ],
  notifications: [],
  rtl: false,
}

const mutations = make.mutations(state);

const actions = {
  fetch: (commit: any) => {
    const local = localStorage.getItem('vuetify@user') || '{}';
    const user = JSON.parse(local);

    for (const key in user) {
      commit(key, user[key]);
    }

    if (user.dark === undefined) {
      commit('dark', window.matchMedia('(prefers-color-scheme: dark)'))
    }
  },
  update: (obj: any) => {
    if (!IN_BROWSER) return;
    let { state } = obj;
    localStorage.setItem('vuetify@user', JSON.stringify(state));
  },
}

const getters = {
  dark: (state: any, getters: any) => {
    return (
      state.dark ||
      getters.gradient.indexOf('255, 255, 255') === -1
    )
  },
  gradient: (state: any) => {
    return state.gradients[state.drawer.gradient];
  },
  image: (state: any) => {
    return state.drawer.image === '' ? state.drawer.image : state.images[state.drawer.image];
  },
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
}
