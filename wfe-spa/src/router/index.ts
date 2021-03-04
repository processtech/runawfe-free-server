import Vue from 'vue';
import Router from 'vue-router';
import { layout } from '../plugins/router-functions';

Vue.use(Router);

const router = new Router({
  routes: [
    layout('Default', [
      // {
      //   name: 'Dashboard',
      //   component: {},
      //   path: ''
      // },

    ]),
  ],
});

export default router;
