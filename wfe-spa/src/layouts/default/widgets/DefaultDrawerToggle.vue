<template>
    <v-btn
        class="ml-3 mr-4"
        elevation="1"
        fab
        small
        @click="toggleMini()"
    >
        <v-icon>
            {{ mini ? 'mdi-format-list-bulleted' : 'mdi-dots-vertical' }}
        </v-icon>
    </v-btn>
</template>

<script lang="ts">
import Vue from 'vue';
import { sync } from 'vuex-pathify';

export default Vue.extend({
    name: "DefaultDrawerToggle",
    computed: {
      mini: sync('app/mini'),
    },
    methods: {
     toggleMini() {
       this.mini = !this.mini;
       localStorage.setItem('mini', this.mini.toString());
     }
    }
});
</script>
