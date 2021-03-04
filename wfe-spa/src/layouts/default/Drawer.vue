<template>
  <v-navigation-drawer
    id="default-drawer"
    v-model="drawer"
    :dark="dark"
    :right="$vuetify.rtl"
    :src="drawerImage ? image : ''"
    :mini-variant.sync="mini"
    mini-variant-width="80"
    app
    width="260"
  >
    <template
      v-if="drawerImage"
      #img="props"
    >
      <v-img
        :key="image"
        :gradient="gradient"
        v-bind="props"
      />
    </template>

    <div class="px-2">
      <default-drawer-header />

      <v-divider class="mx-3 mb-2" />

      <default-list :items="items" />
    </div>

    <template #append>
      <div class="pa-4 text-center">
        <app-btn
          class="text-none mb-4"
          color="white"
          href="https://vuetifyjs.com"
          small
          text
        >
          Documentation
        </app-btn>

        <app-btn
          block
          class="text-none"
          color="secondary"
          href="https://store.vuetifyjs.com/products/vuetify-material-dashboard-pro"
        >
          <v-icon left>
            mdi-package-up
          </v-icon>

          Upgrade to Pro
        </app-btn>
      </div>
    </template>

    <div class="pt-12" />
  </v-navigation-drawer>
</template>

<script lang="ts">
  import Vue from 'vue';
  import DefaultDrawerHeader from './widgets/DrawerHeader.vue';
  import DefaultList from './List.vue';
  import { get, sync } from 'vuex-pathify';

  export default Vue.extend({
    name: 'DefaultDrawer' as string,

    components: {
      DefaultDrawerHeader,
      DefaultList,
    },

    computed: {
      dark: get('user/dark'),
      gradient: get('user/gradient'),
      image: get('user/image'),
      items: get('app/items'),
      //version: get('app/version'),
      drawer: sync('app/drawer'),
      drawerImage: sync('app/drawerImage'),
      mini: sync('app/mini'),
    },

  });
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
