<template>
    <v-list-item class="mb-0 justify-space-between pl-3">
        <v-list-item-avatar>
            <v-img
                :src="
                require('../../../assets/logo.png')"
            />
        </v-list-item-avatar>

        <v-list-item-content class="pl-2">
            <v-list-item-title class="text-h3">
                <strong class="mr-1 font-weight-black">Runa</strong>

                <span class="primary--text">WFE</span>
            </v-list-item-title>
        </v-list-item-content>
    </v-list-item>
</template>

<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
    name: "DefaultDrawerHeader",


});
</script>