<script setup lang="ts">
import { ref, type PropType } from 'vue'
import type { ColorDescription } from '@/ts/color-description'

defineProps({
  colors: {
    type: Array as PropType<ColorDescription[]>,
    required: true,
  }
})

const showDialog = ref(false)
</script>

<template>
  <v-dialog v-model="showDialog" max-width="800px" scroll-strategy="close">
    <template v-slot:activator="{ props }">
      <v-btn icon :variant="showDialog ? 'tonal' : 'text'" v-bind="props">
        <v-icon>mdi-information-outline</v-icon>
      </v-btn>
    </template>
    <v-card class="bg-primary-background">
      <v-card-title class="d-flex align-center">
        <h5>Информация</h5>
        <v-spacer />
        <v-btn icon="mdi-close" flat @click="showDialog = false" />
      </v-card-title>
      <v-divider />
      <v-card-text>
        <v-container>
          <v-row v-for="color in colors" :key="color.value" class="mb-1">
            <v-col cols="4" v-bind:class="'bg-' + color.value" />
            <v-col cols="8">{{ color.description }}</v-col>
          </v-row>
        </v-container>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>
