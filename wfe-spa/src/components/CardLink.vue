<template>
    <div class="d-flex align-center">
        <v-btn text icon @click="$emit('get-item', item);openCard(routeName, item.id)" color="rgba(0, 0, 0, 0.87)">
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
        text: String,
        item: Object
    },
    methods: {
        openCard (routeName: string, id: number) {
            this.$router.push({ name: routeName, params: { id: id.toString() } });
        },
    },
});
</script>
