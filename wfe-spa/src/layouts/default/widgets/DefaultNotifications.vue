<template>
  <v-menu
    bottom
    left
    offset-y
    origin="top right"
    transition="scale-transition"
  >
    <template v-slot:activator="{ attrs, on }">
      <v-btn
        class="ml-2"
        min-width="0"
        text
        v-bind="attrs"
        v-on="on"
      >
        <!-- v-badge
          bordered
          color="red"
          overlap
        >
          <template v-slot:badge>
            <span>{{ notifications.length }}</span>
          </template>

          <v-icon>mdi-bell</v-icon>
        </v-badge -->
      </v-btn>
    </template>

    <v-list
      flat
      nav
    >
      <app-bar-item
        v-for="(n, i) in notifications"
        :key="i"
        link
      >
        <v-list-item-content>
          <v-list-item-title>{{ n }} </v-list-item-title>
        </v-list-item-content>
      </app-bar-item>
    </v-list>
  </v-menu>
</template>

<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
    name: 'DefaultNotifications',
    data: function() {
        return {
            notifications: [
                'Welcome to newweb',
            ],
        }
    },
});
</script>
