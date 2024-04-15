<script setup lang="ts">
import { useThemeStore } from '@/stores/theme-store'
import { accountMenuItems } from '@/static/account-menu-items'
import { computed } from 'vue'

const themeStore = useThemeStore()

const currentTheme = computed({
  get(): string {
    return themeStore.current
  },
  set(themeName: string): void {
    themeStore.switchTo(themeName)
  },
})

const darkTheme = computed({
  get(): boolean {
    return themeStore.dark
  },
  set(): void {
    themeStore.toggleContrast()
  },
})
</script>

<template>
  <v-app-bar app flat dense height="62" color="secondary" class="px-3">
    <v-app-bar-title class="text-primary-text text-h5 mx-2">{{ $route.name }}</v-app-bar-title>
    <v-spacer />
    <v-menu bottom offset-y origin="top right" transition="scale-transition">
      <template v-slot:activator="{ props }">
        <v-btn color="accent" icon="mdi-palette" v-bind="props" />
      </template>
      <v-list class="bg-primary-background">
        <v-list-item>
          <v-radio-group v-model="currentTheme" class="column-wrapper">
            <v-container>
              <v-row no-guttters>
                <v-col class="px-1 py-2" v-for="theme in themeStore.themes" :key="theme.name" cols="6">
                  <v-radio :label="theme.label" :value="theme.name" color="primary" />
                </v-col>
              </v-row>
            </v-container>
          </v-radio-group>
        </v-list-item>
        <v-divider />
        <v-list-item>
          <v-checkbox v-model="darkTheme" label="Темная тема" color="primary" />
        </v-list-item>
      </v-list>
    </v-menu>
    <v-menu bottom offset-y origin="top right" transition="scale-transition">
      <template v-slot:activator="{ props }">
        <v-btn color="accent" icon="mdi-account" v-bind="props" />
      </template>
      <v-list class="bg-primary-background">
        <v-list-item
           v-for="(item, i) in accountMenuItems"
           :to="item.to"
           :key="`list-item-${i}`"
           dense
           link
         >
          <template v-slot:prepend>
            <v-icon :icon="item.icon"></v-icon>
          </template>
          <v-list-item-title v-text="item.title"></v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </v-app-bar>
</template>
