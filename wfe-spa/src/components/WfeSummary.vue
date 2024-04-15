<script setup lang="ts">
import { ref, type PropType } from 'vue'

defineProps({
  title: {
    required: true,
  },
  subtitle: String,
  fields: {
    type: Object as PropType<{ [name: string]: string | undefined }>,
    required: true,
  },
})

const showInfo = ref(false)
</script>

<template>
  <div>
    <v-card class="d-flex justify-space-between align-center bg-primary-background" flat>
      <v-card-title class="d-flex align-center text-primary-text">
        <v-btn icon flat variant="plain" @click="$router.go(-1)" class="mr-2">
          <v-icon>mdi-chevron-double-left</v-icon>
        </v-btn>
        <div>
          <h3>{{ title }}</h3>
        </div>
        <v-btn icon flat variant="plain" class="ml-2" @click="showInfo = !showInfo">
          <v-icon>mdi-information-outline</v-icon>
        </v-btn>
      </v-card-title>
    </v-card>
    <v-expand-transition>
      <v-card v-show="showInfo" flat class="ma-4 bg-primary-background-darken-1 text-primary-text">
        <v-card-title>{{ subtitle }}</v-card-title>
        <v-card-text>
          <div class="mb-1" v-for="(value, name) in fields" :key="name">
            <div v-if="value">
              <span class="d-inline-block" style="width: 20em">{{ name }}: </span>
              <span class="d-inline-block">{{ value }}</span>
            </div>
          </div>
        </v-card-text>
      </v-card>
    </v-expand-transition>
  </div>
</template>
