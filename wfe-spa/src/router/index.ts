import Vue from 'vue';
import Router from 'vue-router';
import { layout } from './router-helpers';
import Desktop from '../views/Desktop.vue';
import TaskList from '../views/TaskList.vue';
import ProcessStart from '../views/ProcessStart.vue';
import ProcessList from '../views/ProcessList.vue';

Vue.use(Router);

const router = new Router({
  routes: [
    layout('Default', [
      {
        name: 'Рабочий стол',
        component: Desktop,
        path: ''
      },
      {
        name: 'Мои задачи',
        component: TaskList,
        path: '/task/list/'
      },
      {
        name: 'Запустить процесс',
        component: ProcessStart,
        path: '/process/start/'
      },
      {
        name: 'Запущенные процессы',
        component: ProcessList,
        path: '/process/list/'
      },
    ]),
  ],
});

export default router;
