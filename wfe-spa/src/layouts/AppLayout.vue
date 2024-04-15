<script setup lang="ts">
import AppBar from '@/layouts/AppBar.vue'
import AppNavigationBar from '@/layouts/AppNavigationBar.vue'
import ScrollableView from '@/components/ScrollableView.vue'
</script>

<template>
  <v-app>
    <scrollable-view class="bg-secondary">
      <v-main class="bg-secondary" style="height: 100%;">
        <app-navigation-bar />
        <app-bar />
        <v-container fluid class="pt-0 pb-4 px-5 bg-secondary full-height">
          <router-view :key="$route.path" />
        </v-container>
      </v-main>
    </scrollable-view>
  </v-app>
</template>
