import TaskList from '@/views/TaskList.vue'
import ProcessDefinitionCard from '@/views/ProcessDefinitionCard.vue'
import ProcessList from '@/views/ProcessList.vue'
import ReportList from '@/views/ReportList.vue'
import ReportCard from '@/views/ReportCard.vue'
import TaskCard from '@/views/TaskCard.vue'
import ProcessCard from '@/views/ProcessCard.vue'
import Profile from '@/views/Profile.vue'
import StartProcess from '@/views/StartProcess.vue'
import { useAuthStore } from '@/stores/auth-store'
import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '@/layouts/Login.vue'
import AppLayout from '@/layouts/AppLayout.vue'
import ChatRooms from '@/views/ChatRooms.vue'
import ProcessChat from '@/views/ProcessChat.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      name: 'Login',
      component: Login,
      path: '/login',
    },
    {
      name: 'AppLayout',
      component: AppLayout,
      path: '',
      redirect: '/task/list/',
      children: [
        {
          name: 'Мои задачи',
          component: TaskList,
          path: '/task/list/',
        },
        {
          name: 'Определения процессов',
          component: StartProcess,
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
        },
        {
          name: 'Отчеты',
          component: ReportList,
          path: '/report/list/'
        },
        {
          name: 'Карточка отчета',
          component: ReportCard,
          path: '/report/:id/card/',
        },
        {
          name: 'Чаты',
          component: ChatRooms,
          path: '/chat/list/',
        },
        {
          name: 'Чат процесса',
          component: ProcessChat,
          path: '/chat/:processId/card/',
        },
      ],
    },
  ],
});

router.beforeEach(function authenticate(to, from, next) {
  if (to.name === 'Login') {
    next()
    return
  }
  const authStore = useAuthStore()
  if (to.path === '/logout') {
    authStore.logout()
    next({ name: 'Login' })
    return
  }
  authStore.authenticate()
    .then(ifAuthenticated => next())
    .catch(ifNotAuthenticated => next({
      name: 'Login',
      query: { forwardUrl: to.fullPath }
    }))
})

export default router;
