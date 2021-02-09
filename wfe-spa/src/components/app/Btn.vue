<template>
  <v-btn
    :color="color"
    v-bind="$attrs"
    v-on="$listeners"
  >
    <slot />
  </v-btn>
</template>

<script lang="ts">
  import Vue from 'vue'
  
  export default Vue.extend({
    name: 'AppBtn' as string,
    props: {
      color: {
        type: String,
        default: 'primary',
      },
    },
  })
</script>
