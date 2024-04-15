<template>
  <div class="bg-primary-background">
    <process-definition-summary :processDefinition="definition" />
      <v-row class="my-0">
        <v-col cols="12">
          <iframe
            :src="oldFormUrl"
            width="100%"
            height="100%"
            frameborder="0"
            style="min-height: 317px;"
          >
            Ваш браузер не поддерживает плавающие фреймы!
          </iframe>
        </v-col>
      </v-row>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import type { WfeProcessDefinition } from '../ts/WfeProcessDefinition'
import { processDefinitionService } from '../services/process-definition-service'
import { mapState } from 'pinia';
import { useSystemStore } from '../stores/system-store'
import { useAuthStore } from '../stores/auth-store';
import ProcessDefinitionSummary from '../components/ProcessDefinitionSummary.vue'

export default defineComponent({
  components: { ProcessDefinitionSummary },

  name: 'ProcessDefinitionCard',

  data: () => ({
    showInfo: false,
    definition: {} as WfeProcessDefinition,
    oldFormUrl: 'about:blank',
  }),

  computed: {
    ...mapState(useAuthStore, ['token']),
    ...mapState(useSystemStore, ['serverUrl']),
  },

  watch: {
    '$route.query.id': function() {
      this.loadDefinition()
    }
  },

  methods: {
    loadDefinition() {
      const id = this.$route.params.id || this.$route.query.id
      processDefinitionService.getDefinitionById(id).then(def => {
        this.definition = Object.assign(this.definition, def)
        this.oldFormUrl = `${this.serverUrl}/wfe/newweboldform.do?id=${def.id}&jwt=${this.token}&startForm=true`
      });
    }
  },

  created() {
    this.loadDefinition()
  },
});
</script>
