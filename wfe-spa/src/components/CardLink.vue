<template>
    <div class="d-flex align-center">
        <v-btn 
            text 
            class="mr-2"
            icon 
            @click="openCard(routeName, id)" 
            color="rgba(0, 0, 0, 0.87)"
        >
            <v-icon>mdi-link</v-icon>
        </v-btn>
        <span>{{ $__ucfirst(text) }}</span>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
    name: "CardLink",
    props: {
        routeName: String,
        id: Number,
        text: String,
    },
    methods: {
        openCard (routeName: string, id: number) {
            this.$router.push({ name: routeName, params: { id: id.toString() } });
        },
    },
});
</script>
