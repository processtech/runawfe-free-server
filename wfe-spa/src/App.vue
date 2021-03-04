<template>
  <v-fade-transition mode="out-in">
    <router-view />
  </v-fade-transition>
</template>

<script lang="ts">
  import Vue from 'vue';
  import './styles/overrides.sass';

  export default Vue.extend({
    name: 'App' as string,
    metaInfo: {
      title: 'Главная',
      titleTemplate: '%s | Runawfe-Professional',
      meta: [
        { charset: 'utf-8' },
        { httpEquiv: 'X-UA-Compatible', content: 'IE=edge' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      ],
    },
  });
</script>
