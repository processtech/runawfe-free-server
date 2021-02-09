<template>
  <v-footer
    id="default-footer"
    color="transparent"
    absolute
    app
    inset
  >
    <links />
  </v-footer>
</template>

<script lang="ts">
  import Vue from 'vue'
  // Components
  import Links from '@/components/Links.vue'

  export default Vue.extend({
    name: 'DefaultFooter' as string,

    components: { Links },
  })
</script>
