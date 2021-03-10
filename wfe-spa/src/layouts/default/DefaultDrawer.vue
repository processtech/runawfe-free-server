<template>
    <v-navigation-drawer
        id="default-drawer"
        v-model="drawer"
        :dark="dark"
        :mini-variant.sync="mini"
        mini-variant-width="80"
        app
        width="260"
    >

    <div class="px-2">
      <default-drawer-header />

      <v-divider class="mx-3 mb-2" />

      <default-list :items="items" />
    </div>

  </v-navigation-drawer>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';
import DefaultDrawerHeader from './widgets/DefaultDrawerHeader.vue';
import DefaultList from './DefaultList.vue';

export default Vue.extend({
    name: "DefaultDrawer",

    components: {
      DefaultDrawerHeader,
      DefaultList,
    },

    computed: {
      dark: get('user/dark'),
      drawer: sync('app/drawer'),
      mini: sync('app/mini'),
      items: get('app/items'),
    },
})
</script>

<style lang="sass">
#default-drawer
  .v-list-item
    margin-bottom: 8px

  .v-list-item::before,
  .v-list-item::after
    display: none

  .v-list-group__header__prepend-icon,
  .v-list-item__icon
    margin-top: 12px
    margin-bottom: 12px
    margin-left: 4px

  &.v-navigation-drawer--mini-variant
    .v-list-item
      justify-content: flex-start !important
</style>