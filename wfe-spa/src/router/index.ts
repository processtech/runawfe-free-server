import Vue from 'vue';
import Router from 'vue-router';
import { trailingSlash } from '../util/helpers';
import { layout } from '../util/routes';
import DashboardView from '../views/Dashboard.vue';
import UserProfileView from '../views/UserProfile.vue';
import NotificationsView from '../views/Notifications.vue';
import IconsView from '../views/Icons.vue';
import TypographyView from '../views/Typography.vue';
import RegularTablesView from '../views/RegularTables.vue';

Vue.use(Router);

const router = new Router({
  // mode: 'history',
  // base: process.env.BASE_URL,
  // scrollBehavior: (to, from, savedPosition) => {
  //   if (to.hash) return { selector: to.hash }
  //   if (savedPosition) return savedPosition

  //   return { x: 0, y: 0 }
  // },
  routes: [
    layout('Default', [
      {
        name: 'Dashboard',
        component: DashboardView,
        path: ''
      },

      // Pages
      {
        name: 'UserProfile',
        component: UserProfileView,
        path: 'components/profile'
      },

      // Components
      {
        name: 'Notifications',
        component: NotificationsView,
        path: 'components/notifications'
      },

      {
        name: 'Icons',
        component: IconsView ,
        path: 'components/icons'
      },

      {
        name: 'Typography',
        component: TypographyView,
        path: 'components/typography'
      },

      // Tables
      {
        name: 'Regular Tables',
        component: RegularTablesView,
        path: 'tables/regular'
      },

    ]),
  ],
});

router.beforeEach((to, from, next) => {
  return to.path.indexOf('/', to.path.length - 1) !== -1 ? next() : next(trailingSlash(to.path));
});

export default router;
