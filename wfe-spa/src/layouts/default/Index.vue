<template>
  <v-app>
    <default-bar></default-bar>
    <default-drawer></default-drawer>
    <default-view></default-view>
    <default-footer></default-footer>
  </v-app>
</template>

<script lang="ts">
  import Vue from 'vue';
  import DefaultView from './DefaultView.vue';
  import DefaultFooter from './DefaultFooter.vue';
  import DefaultBar from './DefaultBar.vue';
  import DefaultDrawer from './DefaultDrawer.vue';
  
  export default Vue.extend({
    name: 'DefaultLayout' as string,

    components: {
      DefaultBar,
      DefaultDrawer,
      DefaultView,
      DefaultFooter,
    },
    
  });
</script>
