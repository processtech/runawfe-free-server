import { make } from 'vuex-pathify';

const state = {
  dark: false,
  notifications: [],
};

const mutations = make.mutations(state);

const actions = {
  update: (args: any) => {
    localStorage.setItem('vuetify@user', JSON.stringify(args.state));
  },
}

const getters = {
  dark: (state: any) => state.dark
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
}
