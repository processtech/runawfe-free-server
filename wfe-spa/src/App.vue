<template>
  <v-fade-transition mode="out-in">
    <router-view />
  </v-fade-transition>
</template>

<script lang="ts">
  import Vue from 'vue'
  // Styles
  import '@/styles/overrides.sass'

  export default Vue.extend({
    name: 'App' as string,
    metaInfo: {
      title: 'App',
      titleTemplate: '%s | Material Dashboard Free',
      htmlAttrs: { lang: 'en' },
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      ],
    },
  })
</script>
