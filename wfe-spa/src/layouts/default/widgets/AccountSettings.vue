<template>
  <default-list
    :items="items"
    class="mb-n2"
  />
</template>

<script lang="ts">
  import Vue from 'vue'

  export default Vue.extend({
    name: 'DefaultAccountSettings' as string,

    components: {
      DefaultList: () => import(
        /* webpackChunkName: "default-list" */
        '../List.vue'
      ),
    },

    data: () => ({
      items: [
        {
          title: 'John Leider',
          icon: 'mdi-vuetify',
          items: [
            {
              title: 'My Profile',
            },
            {
              title: 'Edit Profile',
            },
            {
              title: 'Settings',
            },
          ],
        },
      ],
    }),
  })
</script>
