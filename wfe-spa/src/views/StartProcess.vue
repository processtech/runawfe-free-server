<template>
  <div v-if="processStartFormByRowClick" class="full-height d-flex">
    <v-col cols="4">
      <process-definition-compact-list @rowClick="updateForm" />
    </v-col>
    <v-col cols="8">
      <process-definition-card v-if="showStartForm" />
      <v-card
        v-else
        flat
        class="bg-primary-background full-height d-flex justify-center align-center"
      >
        <v-img
          :src="`${publicPath}/${themeContrast}/process-start.svg`"
          max-width="15rem"
          class="opacity-01"
        />
      </v-card>
    </v-col>
  </div>
  <v-row v-else>
    <v-col cols="12">
      <process-definition-list @rowClick="openStartForm" />
    </v-col>
  </v-row>
</template>

<script lang="ts">
import { mapState } from 'pinia'
import { defineComponent } from 'vue'
import { usePreferencesStore } from '../stores/preferencese-store'
import { useThemeStore } from '../stores/theme-store'
import ProcessDefinitionCompactList from '../views/ProcessDefinitionCompactList.vue'
import ProcessDefinitionCard from '../views/ProcessDefinitionCard.vue'
import ProcessDefinitionList from '../views/ProcessDefinitionList.vue'
import { systemConfiguration } from '@/logic/system-configuration'

export default defineComponent({
  name: 'StartProcess',

  components: {
    ProcessDefinitionCompactList,
    ProcessDefinitionCard,
    ProcessDefinitionList,
  },

  computed: {
    ...mapState(usePreferencesStore, ['processStartFormByRowClick']),
    ...mapState(useThemeStore, ['themeContrast']),

    publicPath: systemConfiguration.publicPath,

    showStartForm(): boolean {
      return this.$route.query.id
    },
  },

  methods: {
    updateForm(id: number): void {
      this.$router.push({
        query: {
          ...this.$route.query,
          id,
        },
      })
    },

    openStartForm(id: number): void {
      this.$router.push(`/process/definition/${id}/card/`)
    },
  },
})
</script>

<style lang="scss">
  .opacity-01 {
    opacity: 0.1;
  }

  .full-height {
    height: 100%
  }
</style>
