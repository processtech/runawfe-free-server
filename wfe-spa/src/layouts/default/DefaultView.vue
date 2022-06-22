<template>
    <v-main>
        <v-container fluid>
            <router-view :key="$route.path" />
        </v-container>
    </v-main>
</template>

<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
    name: "DefaultView",
});
</script>