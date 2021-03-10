<template>
    <v-list
    expand
    nav
    v-bind="$attrs"
    v-on="$listeners"
    >
        <v-list-item v-for="(item, i) in items"
            :key="`item-${i}`"
            :href="item.href"
            :rel="item.href ? 'nofollow' : undefined"
            :target="item.href ? '_blank' : undefined"
            :to="item.to"
            active-class="primary white--text"
            link
            class="py-1"
            v-bind="$attrs"
            v-on="$listeners"
        >
            <v-list-item-icon class="my-2 align-self-center">
                <v-icon v-text="item.icon" />
            </v-list-item-icon>

            <v-list-item-content v-if="item.title">
                <v-list-item-title v-text="item.title" />
            </v-list-item-content>
        </v-list-item>
    </v-list>
</template>

<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
    name: "DefaultList",
    
    props: {
      items: {
        type: Array,
        default: () => ([]),
      },
    },
})
</script>