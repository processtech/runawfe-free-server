<template>
  <v-list-group
    :color="gradient !== 1 ? 'white' : undefined"
    :group="group"
    :prepend-icon="item.icon"
    eager
    v-bind="$attrs"
  >
    <template v-slot:activator>
      <v-list-item-icon
        v-if="!item.icon && !item.avatar"
        class="text-caption text-uppercase text-center my-2 align-self-center"
        style="margin-top: 14px"
      >
        {{ title }}
      </v-list-item-icon>

      <v-list-item-avatar v-if="item.avatar">
        <v-img :src="item.avatar" />
      </v-list-item-avatar>

      <v-list-item-content v-if="item.title">
        <v-list-item-title v-text="item.title" />
      </v-list-item-content>
    </template>

    <template v-for="(child, i) in item.items">
      <default-list-group
        v-if="child.items"
        :key="`sub-group-${i}`"
        :item="child"
      />

      <default-list-item
        v-if="!child.items"
        :key="`child-${i}`"
        :item="child"
      />
    </template>
  </v-list-group>
</template>

<script lang="ts">
  import { get } from 'vuex-pathify';
  import Vue from 'vue';
  import DefaultListItem from './ListItem.vue';

  export default Vue.extend({
    name: 'DefaultListGroup' as string,

    components: {
      DefaultListItem,
    },

    props: {
      item: {
        type: Object,
        default: () => ({}),
      },
    },

    computed: {
      gradient: get('user/drawer@gradient'),
      group () {
        return (this as any).genGroup(this.item.items);
      },
      title () {
        const matches = this.item.title.match(/\b(\w)/g);

        return matches.join('');
      },
    },

    methods: {
      genGroup (items: any) {
        return items.reduce((acc: any, cur: any) => {
          if (!cur.to) return acc;

          acc.push(
            cur.items
              ? (this as any).genGroup(cur.items)
              : cur.to.slice(1, -1),
          );

          return acc;
        }, []).join('|');
      },
    },
  });
</script>
