<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useUiStore } from '@/stores/ui-store'
import { useThemeStore } from '@/stores/theme-store'
import { mainMenuItems } from '@/static/main-menu-items'
import { useSystemStore } from '@/stores/system-store'

const uiStore = useUiStore()

const { mini } = storeToRefs(uiStore)
const { navBarContrast, logo } = storeToRefs(useThemeStore())
const { publicPath } = storeToRefs(useSystemStore())
</script>

<template>
  <v-navigation-drawer
    id="sidebar"
    color="nav-bar"
    :rail="mini"
    rail-width="54"
    app
    width="220"
    floating
  >
    <v-row class="py-4 mx-auto flex-nowrap">
      <v-col cols="2">
        <v-img :src="`${publicPath}/${logo}`" min-width="30" width="30" min-height="30" />
      </v-col>
      <v-col v-if="!mini" cols="10" class="px-4">
        <v-img
          :src="`${publicPath}/${navBarContrast}/logo-text.png`"
          width="120"
          min-width="120"
        />
      </v-col>
    </v-row>
    <v-list class="py-0">
      <v-list-item
        v-for="item in mainMenuItems"
        :key="item.title"
        :to="item.to"
        link
      >
        <template v-slot:prepend>
          <div
            class="my-2 align-self-center justify-center margin-2px-negative"
            :class="{'mr-3' : !mini}"
          >
            <v-img
              :src="`${publicPath}/${navBarContrast}/${item.icon}.png`"
              width="28px"
              min-width="28px"
            />
          </div>
        </template>
        <v-list-item-title v-if="!mini" class="nav-text">
          {{ item.title }}
        </v-list-item-title>
      </v-list-item>
    </v-list>
    <template v-slot:append>
      <v-btn
        color="transparent"
        class="justify-end"
        block
        flat
        @click="uiStore.toggleMini" size="large"
        :append-icon="mini ? 'mdi-chevron-double-right': 'mdi-chevron-double-left'"
      />
      <div
        class="px-4 py-1 text-no-wrap text-caption"
        :class="{ 'opacity-0': mini }"
      >
        &copy; {{ (new Date()).getFullYear() }}, Runawfe Professional
      </div>
    </template>
  </v-navigation-drawer>
</template>

<style lang="scss">
  #sidebar {
    .opacity-0 {
      opacity: 0;
    }

    .nav-text {
      font-size: 0.8rem;
    }

    .margin-2px-negative {
      margin-left: -3px
    }

    .v-list-item--active {
      background-color: rgb(var(--v-theme-primary));
      > .v-list-item__overlay {
        opacity: 0;
      }
    }
  }
</style>
