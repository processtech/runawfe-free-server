<template>
  <v-menu
    bottom
    left
    min-width="200"
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
        <v-icon>mdi-account</v-icon>
      </v-btn>
    </template>

    <v-list
      :tile="false"
      nav
      dense
    >
      <v-list-item-group>
        <template v-for="(item, i) in profile">
          <v-divider
            v-if="item.divider"
            :key="`divider-${i}`"
            class="mb-2 mt-2"
          />
          <v-list-item
            v-else
            :to="item.to"
            :key="`list-item-${i}`"
            dense
            link
          >
            <v-list-item-icon>
              <v-icon color="rgba(0, 0, 0, 0.87)" v-text="item.icon"></v-icon>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title v-text="item.title"></v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </template>
      </v-list-item-group>
    </v-list>
  </v-menu>
</template>

<script lang="ts">
import Vue from 'vue';
import { get } from 'vuex-pathify';

export default Vue.extend({
    name: 'DefaultAccount',
    computed: {
      profile: get('app/profile'),
    }
});
</script>
