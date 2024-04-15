<script setup lang="ts">
import { ref } from 'vue'

const expanded = ref(false)
</script>

<template>
  <div class="expanded">
    <v-expand-transition>
      <div v-show="expanded">
        <slot />
      </div>
    </v-expand-transition>
    <v-expand-transition>
      <div class="expand-cell py-1" @click.stop="expanded = !expanded;">
        <div v-if="expanded">
          <v-icon>mdi-chevron-up</v-icon>
          <span class="text-caption">Свернуть</span>
        </div>
        <div v-else>
          <v-icon>mdi-chevron-down</v-icon>
          <span class="text-caption">Развернуть</span>
        </div>
      </div>
    </v-expand-transition>
  </div>
</template>

<style lang="scss">
  .expanded {
    width: 100%;

    .expand-cell {
      text-align: center;
      cursor: pointer;
      transition: all 1.5s;
    }

    .expand-cell:hover {
      background-color: rgb(var(--v-theme-info));
    }
  }
</style>
