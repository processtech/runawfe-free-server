<template>
  <v-app-bar
    id="default-app-bar"
    app
    absolute
    class="v-bar--underline"
    color="transparent"
    :clipped-left="$vuetify.rtl"
    :clipped-right="!$vuetify.rtl"
    height="70"
    flat
  >
    <v-app-bar-nav-icon
      class="hidden-md-and-up"
      @click="drawer = !drawer"
    />

    <default-drawer-toggle class="hidden-sm-and-down" />

    <v-toolbar-title
      class="font-weight-light text-h5"
      v-text="name"
    />

    <v-spacer />

    <default-search class="hidden-sm-and-down" />

    <default-go-home />

    <default-notifications />

    <default-account />
  </v-app-bar>
</template>

<script lang="ts">
  import Vue from 'vue';
  import DefaultAccount from './widgets/Account.vue';
  import DefaultDrawerToggle from './widgets/DrawerToggle.vue';
  import DefaultGoHome from './widgets/GoHome.vue';
  import DefaultNotifications from './widgets/Notifications.vue';
  import DefaultSearch from './widgets/Search.vue';
  import { get, sync } from 'vuex-pathify';

  export default Vue.extend({
    name: 'DefaultBar' as string,

    components: {
      DefaultAccount,
      DefaultDrawerToggle,
      DefaultGoHome,
      DefaultNotifications,
      DefaultSearch,
    },

    computed: {
      drawer: sync('app/drawer'),
      mini: sync('app/mini'),
      name: get('route/name'),
    }
  });
</script>
