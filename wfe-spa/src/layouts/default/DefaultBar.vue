<template>
    <v-app-bar
        id="default-app-bar"
        app
        absolute
        class="v-bar--underline"
        color="transparent"
        :clipped-right="clippedRight"
        height="70"
        flat
    >
        <v-app-bar-nav-icon
            class="hidden-md-and-up"
            @click="drawer = !drawer"
        />

        <default-drawer-toggle class="hidden-sm-and-down" />

        <v-toolbar-title
            class="font-weight-light text-h5"
            v-text="name"
        />

    </v-app-bar>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';
import DefaultDrawerToggle from './widgets/DefaultDrawerToggle.vue';

export default Vue.extend({
    name: "DefaultBar",
    data: function() {
        return { clippedRight: true }
    },
    computed: {
      drawer: sync('app/drawer'),
      mini: sync('app/mini'),
      name: get('route/name'),
    },
    components: {
        DefaultDrawerToggle,
    }
});
</script>