<template>
  <v-btn
    class="ml-2"
    min-width="0"
    text
    to="/"
    exact
  >
    <v-icon>mdi-view-dashboard</v-icon>
  </v-btn>
</template>

<script lang="ts">
  import Vue from 'vue'

  export default Vue.extend({
    name: 'DefaultGoHome' as string,
  })
</script>
