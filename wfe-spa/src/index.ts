// Polyfills needed for vuetify
import 'core-js/stable'
import 'regenerator-runtime/runtime'
// Imports
import Vue from 'vue';
import './plugins/utils';
import App from './App.vue';
import router from './router';
import vuetify from './plugins/vuetify';
import './plugins/component-loader';
import './plugins/vue-meta';
// import './plugins/swagger-client';
import store from './store';
import { sync } from 'vuex-router-sync';

sync(store, router);

new Vue({
  router,
  vuetify,
  store,
  render: h => h(App),
}).$mount('#app');
