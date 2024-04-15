<template>
  <router-view />
</template>

<script lang="ts">
import { mapActions } from 'pinia'
import { defineComponent } from 'vue'
import { useThemeStore } from './stores/theme-store'

export default defineComponent({
  name: 'App',

  created() {
    this.initTheming()
  },

  methods: {
    ...mapActions(useThemeStore, { initTheming: 'init' })
  },
});
</script>
