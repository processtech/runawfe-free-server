import Vue from 'vue';
import Router from 'vue-router';
import { layout } from '../plugins/router-functions';
import Desktop from '../views/Desktop.vue';
import TaskList from '../views/TaskList.vue';
import ProcessStart from '../views/ProcessStart.vue';
import ProcessList from '../views/ProcessList.vue';

Vue.use(Router);

const router = new Router({
  routes: [
    layout('Default', [
      {
        name: 'Desktop',
        component: Desktop,
        path: ''
      },
      {
        name: 'TaskList',
        component: TaskList,
        path: '/task/list/'
      },
      {
        name: 'ProcessStart',
        component: ProcessStart,
        path: '/process/start/'
      },
      {
        name: 'ProcessList',
        component: ProcessList,
        path: '/process/list/'
      },
    ]),
  ],
});

export default router;
