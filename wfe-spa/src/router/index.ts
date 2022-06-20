import Vue from 'vue';
import VueRouter from 'vue-router';
import { layout } from './router-helpers';
import Desktop from '../views/Desktop.vue';
import TaskList from '../views/TaskList.vue';
import ProcessDefinitionList from '../views/ProcessDefinitionList.vue';
import ProcessDefinitionCard from '../views/ProcessDefinitionCard.vue';
import ProcessList from '../views/ProcessList.vue';
import TaskCard from '../views/TaskCard.vue';
import ProcessCard from '../views/ProcessCard.vue';
import Profile from '../views/Profile.vue';
import store from '../store';

Vue.use(VueRouter);

const router = new VueRouter({
  routes: [
    layout('Auth'),
    layout('Default', [
      // {
      //   name: 'Рабочий стол',
      //   component: Desktop,
      //   path: '',
      // },
      {
        name: 'Мои задачи',
        component: TaskList,
        path: '/task/list/',
      },
      {
        name: 'Определения процессов',
        component: ProcessDefinitionList,
        path: '/process/definition/list/',
      },
      {
        name: 'ProcessDefinitionCard',
        component: ProcessDefinitionCard,
        path: '/process/definition/:id/card/',
      },
      {
        name: 'Запущенные процессы',
        component: ProcessList,
        path: '/process/list/',
      },
      {
        name: 'Карточка задачи',
        component: TaskCard,
        path: '/task/:id/card/',
      },
      {
        name: 'Карточка процесса',
        component: ProcessCard,
        path: '/process/:id/card/',
      },
      {
        name: 'Профиль',
        component: Profile,
        path: '/profile/'
      }
    ]),
  ],
});

// Здесь глобальный хук, чтобы проверять авторизацию всех маршрутах, кроме /login
router.beforeEach((to, from, next) => {
  if (to.path == '/logout') {
    store.dispatch('user/logout');
    next({ name: 'Login' });
  } else if (to.name !== 'Login') {
    store.dispatch('user/authenticate').then(ifAuthenticated => {
      next();
    }, ifNotAuthenticated => {
      next({ name: 'Login' });
    }).catch((error: any) => {
      console.log(error);
    });
  } else {
    next();
  }
});

export default router;
