<template>
  <v-list
    expand
    nav
    v-bind="$attrs"
    v-on="$listeners"
  >
    <template v-for="(item, i) in items">
      <default-list-group
        v-if="item.items"
        :key="`group-${i}`"
        :item="item"
      />

      <default-list-item
        v-else
        :key="`item-${i}`"
        :item="item"
      />
    </template>
  </v-list>
</template>

<script lang="ts">
  import Vue from 'vue'

  export default Vue.extend({
    name: 'DefaultList' as string,

    components: {
      DefaultListGroup: () => import('./ListGroup.vue'),
      DefaultListItem: () => import('./ListItem.vue'),
    },

    props: {
      items: {
        type: Array,
        default: () => ([]),
      },
    },
  })
</script>
