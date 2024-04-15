<script setup lang="ts">
import { computed, ref, type ComputedRef } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const emit = defineEmits<{(e: 'toggleFilter'): void}>()

const showFilter = ref(false)

const filterApplied: ComputedRef<boolean> = computed(() => Boolean(route.query.filter))

function toggleFilter(): void {
  showFilter.value = !showFilter.value
  emit('toggleFilter')
}
</script>

<template>
  <div class="">
    <v-btn @click="toggleFilter" :variant="showFilter ? 'tonal' : 'text'" icon="mdi-filter" />
    <v-btn
      icon="mdi-close"
      :disabled="!filterApplied"
      @click="() => $router.push({ query: { ...$route.query, filter: '' }})"
    />
  </div>
</template>
