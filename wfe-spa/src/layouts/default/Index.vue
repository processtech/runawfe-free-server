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
  import Vue from 'vue'

  export default Vue.extend({
    name: 'DefaultLayout' as string,

    components: {
      DefaultBar: () => import(
        /* webpackChunkName: "default-app-bar" */
        './AppBar.vue'
      ),
      DefaultDrawer: () => import(
        /* webpackChunkName: "default-drawer" */
        './Drawer.vue'
      ),
      DefaultFooter: () => import(
        /* webpackChunkName: "default-footer" */
        './Footer.vue'
      ),
      DefaultSettings: () => import(
        /* webpackChunkName: "default-settings" */
        './Settings.vue'
      ),
      DefaultView: () => import(
        /* webpackChunkName: "default-view" */
        './View.vue'
      ),
    },
  })
</script>
