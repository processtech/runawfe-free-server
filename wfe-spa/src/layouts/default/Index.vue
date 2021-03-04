<template>
  <v-app>
    <default-bar />

    <default-drawer />

    <default-view />

    <default-footer />

    <default-settings />
  </v-app>
</template>

<script lang="ts">
  import Vue from 'vue';
  import DefaultBar from './AppBar.vue';
  import DefaultDrawer from './Drawer.vue';
  import DefaultFooter from './Footer.vue';
  import DefaultSettings from './Settings.vue';
  import DefaultView from './View.vue';

  export default Vue.extend({
    name: 'DefaultLayout' as string,

    components: {
      DefaultBar,
      DefaultDrawer,
      DefaultFooter,
      DefaultSettings,
      DefaultView,
    },
  });
</script>
