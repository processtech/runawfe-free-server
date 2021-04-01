<template>
    <div class="d-flex align-center">
        <v-btn text icon @click="$emit('get-id', id);openCard(routeName, id)" color="primary">
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
        text: String
    },
    methods: {
        openCard (routeName: string, id: any) {
            this.$router.push({ name: routeName, params: { id: id }})
        },
    },
});
</script>
