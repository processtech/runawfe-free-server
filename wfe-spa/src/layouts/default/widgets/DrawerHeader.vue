<template>
  <v-list-item class="mb-0 justify-space-between pl-3">
    <v-list-item-avatar>
      <v-img
        :src="
          require('@/assets/vmd.svg')"
      />
    </v-list-item-avatar>

    <v-list-item-content class="pl-2">
      <v-list-item-title class="text-h3">
        <strong class="mr-1 font-weight-black">VMD</strong>

        <span class="primary--text">FREE</span>
      </v-list-item-title>
    </v-list-item-content>
  </v-list-item>
</template>

<script lang="ts">
  import Vue from 'vue'
  // Utilities
  import { get } from 'vuex-pathify'

  export default Vue.extend({
    name: 'DefaultDrawerHeader' as string,

    computed: { version: get('app/version') },
  })
</script>
