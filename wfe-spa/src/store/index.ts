import Vue from 'vue';
import Vuex from 'vuex';
import pathify from '../plugins/vuex-pathify';
import app from './modules/app';
import user from './modules/user';

Vue.use(Vuex);

const store = new Vuex.Store({
  modules: {
    app,
    user,
  },
  plugins: [
    pathify.plugin,
  ],
});

store.dispatch('app/init');
store.dispatch('user/fetch');

export default store;
